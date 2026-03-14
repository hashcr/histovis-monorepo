package org.histovis.imagesservice.config;

import io.minio.MinioClient;
import org.histovis.imagesservice.storage.MinioObjectStorageService;
import org.histovis.imagesservice.storage.ObjectStorageService;
import org.histovis.imagesservice.storage.S3ObjectStorageService;
import org.histovis.imagesservice.storage.StorageProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Bean
    @ConditionalOnProperty(name = "storage.type", havingValue = "minio", matchIfMissing = true)
    public ObjectStorageService minioObjectStorageService(MinioClient minioClient, StorageProperties properties) {
        return new MinioObjectStorageService(minioClient, properties);
    }

    @Bean
    @ConditionalOnProperty(name = "storage.type", havingValue = "s3")
    public ObjectStorageService s3ObjectStorageService(StorageProperties properties) {
        return new S3ObjectStorageService(properties);
    }
}
