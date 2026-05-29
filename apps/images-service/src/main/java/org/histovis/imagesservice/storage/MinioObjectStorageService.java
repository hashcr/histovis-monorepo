package org.histovis.imagesservice.storage;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.histovis.imagesservice.exception.StorageException;

import java.io.InputStream;

@Slf4j
public class MinioObjectStorageService implements ObjectStorageService {

    private final MinioClient minioClient;
    private final StorageProperties properties;

    public MinioObjectStorageService(MinioClient minioClient, StorageProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

    @Override
    public String upload(String key, InputStream data, long size, String contentType) {
        String bucket = properties.getMinio().getBucket();
        try {
            log.info("Uploading object to MinIO: bucket={}, key={}", bucket, key);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .stream(data, size, -1)
                            .contentType(contentType)
                            .build()
            );
            String url = getUrl(key);
            log.info("Upload successful: {}", url);
            return url;
        } catch (Exception e) {
            log.error("Failed to upload object to MinIO: bucket={}, key={}", bucket, key, e);
            throw new StorageException("Failed to upload object: " + key, e);
        }
    }

    @Override
    public void delete(String key) {
        String bucket = properties.getMinio().getBucket();
        try {
            log.info("Deleting object from MinIO: bucket={}, key={}", bucket, key);
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to delete object from MinIO: bucket={}, key={}", bucket, key, e);
            throw new StorageException("Failed to delete object: " + key, e);
        }
    }

    @Override
    public String getUrl(String key) {
        String publicEndpoint = properties.getMinio().getPublicEndpoint();
        if (publicEndpoint != null && !publicEndpoint.isBlank()) {
            return publicEndpoint + "/" + key;
        }
        String bucket = properties.getMinio().getBucket();
        return properties.getMinio().getEndpoint() + "/" + bucket + "/" + key;
    }
}
