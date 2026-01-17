package com.danp1t.service;

import com.danp1t.config.MinioConfig;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Tags;
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

    // Существующий метод для обратной совместимости
    public void uploadFile(InputStream inputStream, String originalFileName,
                           String contentType, Long userId) throws IOException {
        uploadFileWithKey(inputStream, generateFileName(originalFileName, userId),
                contentType, userId, originalFileName);
    }

    // Новый метод для сохранения с указанным ключом
    public void uploadFileWithKey(InputStream inputStream, String objectKey,
                                  String contentType, Long userId, String originalFileName) throws IOException {
        try {
            MinioClient minioClient = minioConfig.getClient();

            // Создаем метаданные
            Map<String, String> metadata = new HashMap<>();
            metadata.put("original-filename", originalFileName);
            metadata.put("upload-date", LocalDateTime.now().toString());
            metadata.put("user-id", userId.toString());

            // Получаем размер потока (если возможно)
            int available = inputStream.available();
            long objectSize = (available > 0) ? available : -1;

            // Загружаем файл под указанным ключом
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectKey)
                            .stream(inputStream, objectSize, -1)
                            .contentType(contentType)
                            .userMetadata(metadata)
                            .build()
            );

        } catch (Exception e) {
            throw new IOException("Ошибка загрузки файла в MinIO", e);
        }
    }

    // Остальные методы остаются без изменений...
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

    public void copyFile(String sourceKey, String destinationKey) throws IOException {
        try {
            MinioClient minioClient = minioConfig.getClient();

            CopySource source = CopySource.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(sourceKey)
                    .build();

            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(destinationKey)
                            .source(source)
                            .build()
            );
        } catch (Exception e) {
            throw new IOException("Failed to copy file: " + e.getMessage(), e);
        }
    }

    /**
     * Добавляет теги к файлу
     */
    public void addFileTags(String fileKey, String key, String value) throws IOException {
        try {
            MinioClient minioClient = minioConfig.getClient();

            // Сначала получаем существующие теги
            Tags existingTags = minioClient.getObjectTags(
                    GetObjectTagsArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(fileKey)
                            .build()
            );

            // Создаем новый Map с существующими тегами
            Map<String, String> tags = new HashMap<>();
            if (existingTags != null) {
                existingTags.get().forEach(tags::put);
            }

            // Добавляем/обновляем тег
            tags.put(key, value);

            minioClient.setObjectTags(
                    SetObjectTagsArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(fileKey)
                            .tags(tags)
                            .build()
            );
        } catch (Exception e) {
            throw new IOException("Failed to add tags to file: " + e.getMessage(), e);
        }
    }

    /**
     * Получает теги файла
     */
    public Map<String, String> getFileTags(String fileKey) throws IOException {
        try {
            MinioClient minioClient = minioConfig.getClient();

            Tags tags = minioClient.getObjectTags(
                    GetObjectTagsArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(fileKey)
                            .build()
            );

            Map<String, String> result = new HashMap<>();
            if (tags != null) {
                tags.get().forEach((k, v) -> result.put(k, v));
            }
            return result;
        } catch (Exception e) {
            throw new IOException("Failed to get file tags: " + e.getMessage(), e);
        }
    }

    /**
     * Перемещает файл (копирует и удаляет оригинал)
     */
    public void moveFile(String sourceKey, String destinationKey) throws IOException {
        copyFile(sourceKey, destinationKey);
        deleteFile(sourceKey);
    }
}