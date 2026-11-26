package org.histovis.commons.storage;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.SetBucketPolicyArgs;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.histovis.commons.exception.StorageException;

import java.io.InputStream;

@Slf4j
public class MinioObjectStorageService implements ObjectStorageService {

    private final MinioClient minioClient;
    private final StorageProperties properties;
    private volatile boolean bucketInitialized = false;

    public MinioObjectStorageService(MinioClient minioClient, StorageProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        try {
            ensureBucketReady();
        } catch (Exception e) {
            log.warn("Deferred MinIO bucket initialization during startup (will retry on first upload): {}", e.getMessage());
        }
    }

    private synchronized void ensureBucketReady() {
        if (bucketInitialized) {
            return;
        }
        String bucket = properties.getMinio().getBucket();
        if (bucket == null || bucket.isBlank()) {
            return;
        }
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                try {
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                    log.info("Created MinIO bucket: {}", bucket);
                } catch (Exception createEx) {
                    if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                        throw createEx;
                    }
                }
            }

            String publicReadPolicy = """
                    {
                      "Version": "2012-10-17",
                      "Statement": [
                        {
                          "Effect": "Allow",
                          "Principal": {"AWS": ["*"]},
                          "Action": ["s3:GetBucketLocation"],
                          "Resource": ["arn:aws:s3:::%s"]
                        },
                        {
                          "Effect": "Allow",
                          "Principal": {"AWS": ["*"]},
                          "Action": ["s3:GetObject"],
                          "Resource": ["arn:aws:s3:::%s/*"]
                        }
                      ]
                    }
                    """.formatted(bucket, bucket);

            try {
                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(bucket)
                                .config(publicReadPolicy)
                                .build()
                );
                log.info("Ensured public read policy on MinIO bucket: {}", bucket);
            } catch (Exception policyEx) {
                log.warn("Could not apply S3 bucket policy on '{}' (expected if using Cloudflare R2 or external policy management): {}",
                        bucket, policyEx.getMessage());
            }

            bucketInitialized = true;
        } catch (Exception e) {
            throw new StorageException("Failed to initialize bucket: " + bucket, e);
        }
    }

    @Override
    public String upload(String key, InputStream data, long size, String contentType) {
        ensureBucketReady();
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
