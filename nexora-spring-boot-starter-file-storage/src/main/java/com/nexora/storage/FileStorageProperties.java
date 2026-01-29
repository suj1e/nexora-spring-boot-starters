package com.nexora.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * File storage configuration properties.
 *
 * <p>Configuration example:
 * <pre>
 * nexora:
 *   file-storage:
 *     type: oss
 *     upload-path: /data/uploads
 *     base-url: https://cdn.example.com
 *     max-file-size: 10MB
 *     allowed-extensions: jpg,jpeg,png,pdf
 *     oss:
 *       endpoint: oss-cn-hangzhou.aliyuncs.com
 *       bucket: my-bucket
 * </pre>
 *
 * @author sujie
 */
@Data
@ConfigurationProperties(prefix = "nexora.file-storage")
public class FileStorageProperties {

    /**
     * Storage type: local, oss, s3, minio.
     */
    private StorageType type = StorageType.LOCAL;

    /**
     * Upload path for local storage.
     */
    private String uploadPath = "/data/uploads";

    /**
     * Base URL for file access (CDN or domain).
     */
    private String baseUrl;

    /**
     * Maximum file size for upload.
     */
    private DataSize maxFileSize = new DataSize(10, DataSizeUnit.MB);

    /**
     * Allowed file extensions (empty means all allowed).
     * Example: jpg,jpeg,png,pdf,doc,docx
     */
    private String allowedExtensions;

    /**
     * Enable file hashing for deduplication.
     */
    private boolean enableHash = true;

    /**
     * Hash algorithm: MD5, SHA-256, SHA-512.
     */
    private HashAlgorithm hashAlgorithm = HashAlgorithm.SHA256;

    /**
     * Enable date-based path partitioning (e.g., uploads/2024/01/15/).
     */
    private boolean enableDatePath = true;

    /**
     * Enable UUID filename (to avoid conflicts).
     */
    private boolean enableUuidFilename = true;

    /**
     * Keep original filename as prefix.
     */
    private boolean keepOriginalFilename = false;

    /**
     * URL expiration time for presigned URLs (for cloud storage).
     */
    private Duration urlExpiration = Duration.ofHours(1);

    /**
     * OSS configuration.
     */
    private Oss oss = new Oss();

    /**
     * S3 configuration.
     */
    private S3 s3 = new S3();

    /**
     * MinIO configuration.
     */
    private Minio minio = new Minio();

    /**
     * Storage type enum.
     */
    public enum StorageType {
        LOCAL, OSS, S3, MINIO
    }

    /**
     * Hash algorithm enum.
     */
    public enum HashAlgorithm {
        MD5, SHA256, SHA512
    }

    /**
     * Data size class.
     */
    public static class DataSize {
        private final long value;
        private final DataSizeUnit unit;

        public DataSize(long value, DataSizeUnit unit) {
            this.value = value;
            this.unit = unit;
        }

        public long toBytes() {
            return value * unit.toBytes();
        }
    }

    /**
     * Data size unit.
     */
    public enum DataSizeUnit {
        KB(1024), MB(1024 * 1024), GB(1024 * 1024 * 1024);

        private final long bytes;

        DataSizeUnit(long bytes) {
            this.bytes = bytes;
        }

        public long toBytes() {
            return bytes;
        }
    }

    /**
     * Aliyun OSS configuration.
     */
    @Data
    public static class Oss {
        /**
         * OSS endpoint.
         */
        private String endpoint;

        /**
         * Access key ID.
         */
        private String accessKeyId;

        /**
         * Access key secret.
         */
        private String accessKeySecret;

        /**
         * Bucket name.
         */
        private String bucket;

        /**
         * Region (optional, can be parsed from endpoint).
         */
        private String region;

        /**
         * Role ARN for STS (optional).
         */
        private String roleArn;

        /**
         * Role session name for STS (optional).
         */
        private String roleSessionName;
    }

    /**
     * AWS S3 configuration.
     */
    @Data
    public static class S3 {
        /**
         * S3 region.
         */
        private String region;

        /**
         * Access key ID.
         */
        private String accessKeyId;

        /**
         * Secret access key.
         */
        private String secretAccessKey;

        /**
         * Bucket name.
         */
        private String bucket;

        /**
         * Endpoint (for S3-compatible services).
         */
        private String endpoint;
    }

    /**
     * MinIO configuration.
     */
    @Data
    public static class Minio {
        /**
         * MinIO endpoint.
         */
        private String endpoint;

        /**
         * Access key.
         */
        private String accessKey;

        /**
         * Secret key.
         */
        private String secretKey;

        /**
         * Bucket name.
         */
        private String bucket;

        /**
         * Region (optional).
         */
        private String region = "us-east-1";

        /**
         * Enable secure connection (HTTPS).
         */
        private boolean secure = false;
    }
}
