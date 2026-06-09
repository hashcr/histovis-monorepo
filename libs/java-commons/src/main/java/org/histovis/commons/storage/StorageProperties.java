package org.histovis.commons.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    private String type = "minio";
    private Minio minio = new Minio();

    @Data
    public static class Minio {
        private String endpoint;
        private String publicEndpoint;
        private String accessKey;
        private String secretKey;
        private String bucket;
    }
}
