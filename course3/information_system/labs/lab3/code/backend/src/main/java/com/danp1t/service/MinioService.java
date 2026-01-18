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
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class MinioService {

    @Inject
    private MinioConfig minioConfig;

    public void uploadFileWithKey(InputStream inputStream, String objectKey,
                                  String contentType, Long userId, String originalFileName) throws IOException {
        try {
            MinioClient minioClient = minioConfig.getClient();

            Map<String, String> metadata = new HashMap<>();
            metadata.put("original-filename", originalFileName);
            metadata.put("upload-date", LocalDateTime.now().toString());
            metadata.put("user-id", userId.toString());

            int available = inputStream.available();
            long objectSize = (available > 0) ? available : -1;

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

    public void addFileTags(String fileKey, String key, String value) throws IOException {
        try {
            MinioClient minioClient = minioConfig.getClient();

            Tags existingTags = minioClient.getObjectTags(
                    GetObjectTagsArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(fileKey)
                            .build()
            );

            Map<String, String> tags = new HashMap<>();
            if (existingTags != null) {
                existingTags.get().forEach(tags::put);
            }

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
}