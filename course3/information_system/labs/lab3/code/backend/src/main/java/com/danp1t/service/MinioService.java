package com.danp1t.service;

import com.danp1t.config.MinioConfig;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class MinioService {

    @Inject
    private MinioConfig minioConfig;

    public String uploadFile(InputStream inputStream, String originalFileName,
                             String contentType, Long userId) throws IOException {
        try {
            MinioClient minioClient = minioConfig.getClient();

            // Генерируем уникальное имя файла
            String uniqueFileName = generateFileName(originalFileName, userId);

            // Создаем метаданные
            Map<String, String> metadata = new HashMap<>();
            metadata.put("original-filename", originalFileName);
            metadata.put("upload-date", LocalDateTime.now().toString());
            metadata.put("user-id", userId.toString());

            // Получаем размер потока (если возможно)
            int available = inputStream.available();
            long objectSize = (available > 0) ? available : -1;

            // Загружаем файл
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(uniqueFileName)
                            .stream(inputStream, objectSize, -1)
                            .contentType(contentType)
                            .userMetadata(metadata)
                            .build()
            );

            return uniqueFileName;
        } catch (Exception e) {
            throw new IOException("Ошибка загрузки файла в MinIO", e);
        }
    }

    public InputStream downloadFile(String fileName) throws IOException {
        try {
            MinioClient minioClient = minioConfig.getClient();

            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(fileName)
                            .build()
            );
        } catch (Exception e) {
            throw new IOException("Ошибка скачивания файла из MinIO", e);
        }
    }

    public String getPresignedUrl(String fileName, int expiryMinutes) throws IOException {
        try {
            MinioClient minioClient = minioConfig.getClient();

            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioConfig.getBucketName())
                            .object(fileName)
                            .expiry(expiryMinutes, TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e) {
            throw new IOException("Ошибка получения URL для файла", e);
        }
    }

    public Map<String, String> getFileMetadata(String fileName) throws IOException {
        try {
            MinioClient minioClient = minioConfig.getClient();

            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(fileName)
                            .build()
            );

            return stat.userMetadata();
        } catch (Exception e) {
            throw new IOException("Ошибка получения метаданных файла", e);
        }
    }

    public void deleteFile(String fileName) throws IOException {
        try {
            MinioClient minioClient = minioConfig.getClient();

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(fileName)
                            .build()
            );
        } catch (Exception e) {
            throw new IOException("Ошибка удаления файла из MinIO", e);
        }
    }

    private String generateFileName(String originalFileName, Long userId) {
        String timestamp = LocalDateTime.now().toString().replaceAll("[^0-9]", "");
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String fileExtension = "";

        int dotIndex = originalFileName.lastIndexOf(".");
        if (dotIndex > 0) {
            fileExtension = originalFileName.substring(dotIndex);
        }

        return String.format("import-%s-%s-%s%s",
                userId, timestamp, uuid, fileExtension);
    }
}