package com.danp1t.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.InputStream;
import java.io.IOException;

@ApplicationScoped
public class TransactionalFileService {

    @Inject
    private MinioService minioService;

    public String saveFileWithTransaction(InputStream inputStream, String fileName,
                                          String contentType, Long userId, Long operationId) throws IOException {

        String tempKey = "pending_" + System.currentTimeMillis() + "_" + operationId + "_" + fileName;
        minioService.uploadFileWithKey(inputStream, tempKey, contentType, userId, fileName);
        minioService.addFileTags(tempKey, "status", "pending");
        minioService.addFileTags(tempKey, "operation_id", String.valueOf(operationId));

        return tempKey;
    }

    public void confirmFileSave(String tempKey) throws IOException {
        if (tempKey == null || !tempKey.startsWith("pending_")) {
            return;
        }
        String permanentKey = tempKey.replaceFirst("pending_", "");
        minioService.copyFile(tempKey, permanentKey);
        minioService.deleteFile(tempKey);
        minioService.addFileTags(permanentKey, "status", "confirmed");
    }

    public void rollbackFileSave(String tempKey) throws IOException {
        if (tempKey == null || !tempKey.startsWith("pending_")) {
            return;
        }

        try {
            minioService.deleteFile(tempKey);
        } catch (Exception e) {
            System.err.println("Failed to rollback file: " + e.getMessage());
        }
    }

    public String getPermanentKey(String tempKey) {
        if (tempKey == null || !tempKey.startsWith("pending_")) {
            return tempKey;
        }
        return tempKey.replaceFirst("pending_", "");
    }
}