package org.histovis.imagesservice.storage;

import java.io.InputStream;

/**
 * AWS S3 implementation of ObjectStorageService.
 *
 * To activate: set storage.type=s3 in configuration.
 *
 * To implement: add the AWS SDK dependency to pom.xml:
 *   <dependency>
 *     <groupId>software.amazon.awssdk</groupId>
 *     <artifactId>s3</artifactId>
 *     <version>2.x.x</version>
 *   </dependency>
 *
 * S3 configuration properties mirror MinIO:
 *   storage.s3.endpoint, storage.s3.access-key, storage.s3.secret-key, storage.s3.bucket
 */
public class S3ObjectStorageService implements ObjectStorageService {

    public S3ObjectStorageService(StorageProperties properties) {
        // Initialize AWS S3 client from properties
    }

    @Override
    public String upload(String key, InputStream data, long size, String contentType) {
        throw new UnsupportedOperationException("S3 storage is not yet configured. Set storage.type=minio or implement this class with the AWS SDK.");
    }

    @Override
    public void delete(String key) {
        throw new UnsupportedOperationException("S3 storage is not yet configured. Set storage.type=minio or implement this class with the AWS SDK.");
    }

    @Override
    public String getUrl(String key) {
        throw new UnsupportedOperationException("S3 storage is not yet configured. Set storage.type=minio or implement this class with the AWS SDK.");
    }
}
