package com.danp1t.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public String storeFile(MultipartFile file, String subDirectory) throws IOException {
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID() + fileExtension;

        Path targetLocation = Paths.get(uploadDir, subDirectory).toAbsolutePath().normalize();
        System.out.printf("Storing file: %s\n", targetLocation);
        Files.createDirectories(targetLocation);
        targetLocation = targetLocation.resolve(fileName);

        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return subDirectory + "/" + fileName;
    }

    public boolean deleteFile(String filePath) {
        try {
            String normalizedPath = filePath.replace('/', FileSystems.getDefault().getSeparator().charAt(0));
            Path path = Paths.get(uploadDir, normalizedPath).toAbsolutePath().normalize();
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            return false;
        }
    }
}