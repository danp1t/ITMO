package com.danp1t.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class MinioConfig {

    @Inject
    @ConfigProperty(name = "minio.endpoint", defaultValue = "http://localhost:20046")
    String endpoint;

    @Inject
    @ConfigProperty(name = "minio.accessKey", defaultValue = "minioadmin")
    String accessKey;

    @Inject
    @ConfigProperty(name = "minio.secretKey", defaultValue = "minioadmin")
    String secretKey;

    @Inject
    @ConfigProperty(name = "minio.bucket.imports", defaultValue = "import-files")
    String bucketName;

    private MinioClient minioClient;

    public void init() {
        try {
            this.minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();

            // Исправленные вызовы
            boolean found = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!found) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка инициализации MinIO клиента", e);
        }
    }

    public MinioClient getClient() {
        if (minioClient == null) {
            init();
        }
        return minioClient;
    }

    public String getBucketName() {
        return bucketName;
    }
}