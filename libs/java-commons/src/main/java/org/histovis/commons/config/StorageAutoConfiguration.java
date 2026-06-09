package org.histovis.commons.config;

import io.minio.MinioClient;
import org.histovis.commons.storage.MinioObjectStorageService;
import org.histovis.commons.storage.ObjectStorageService;
import org.histovis.commons.storage.StorageProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(MinioClient.class)
    @ConditionalOnProperty(name = "storage.minio.endpoint")
    public MinioClient minioClient(StorageProperties properties) {
        return MinioClient.builder()
                .endpoint(properties.getMinio().getEndpoint())
                .credentials(properties.getMinio().getAccessKey(), properties.getMinio().getSecretKey())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(ObjectStorageService.class)
    @ConditionalOnBean(MinioClient.class)
    public ObjectStorageService minioObjectStorageService(MinioClient minioClient, StorageProperties properties) {
        return new MinioObjectStorageService(minioClient, properties);
    }
}
