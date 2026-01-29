package com.nexora.storage.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.nexora.storage.FileMetadata;
import com.nexora.storage.FileStorageProperties;
import com.nexora.storage.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * Aliyun OSS storage implementation.
 *
 * <p>Requires com.aliyun.oss:aliyun-sdk-oss dependency.
 *
 * @author sujie
 */
@Slf4j
@Service
@ConditionalOnClass(OSS.class)
@ConditionalOnProperty(prefix = "nexora.file-storage", name = "type", havingValue = "oss")
@EnableConfigurationProperties(FileStorageProperties.class)
public class OssStorage implements FileStorageService {

    private final FileStorageProperties properties;
    private OSS ossClient;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    public OssStorage(FileStorageProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        FileStorageProperties.Oss ossConfig = properties.getOss();
        if (ossConfig == null || !StringUtils.hasText(ossConfig.getEndpoint()) ||
                !StringUtils.hasText(ossConfig.getAccessKeyId()) ||
                !StringUtils.hasText(ossConfig.getAccessKeySecret()) ||
                !StringUtils.hasText(ossConfig.getBucket())) {
            throw new IllegalArgumentException("OSS configuration is incomplete");
        }

        ossClient = new OSSClientBuilder().build(
                ossConfig.getEndpoint(),
                ossConfig.getAccessKeyId(),
                ossConfig.getAccessKeySecret()
        );

        // Check if bucket exists
        if (!ossClient.doesBucketExist(ossConfig.getBucket())) {
            log.warn("OSS bucket does not exist: {}", ossConfig.getBucket());
        }

        log.info("OSS storage initialized with endpoint: {}, bucket: {}",
                ossConfig.getEndpoint(), ossConfig.getBucket());
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }

    @Override
    public FileMetadata upload(MultipartFile file, String path) throws IOException {
        String filename = generateFilename(file.getOriginalFilename());
        String fileKey = buildPath(path, filename);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        ossClient.putObject(properties.getOss().getBucket(), fileKey, file.getInputStream(), metadata);

        log.info("File uploaded to OSS: {}", fileKey);

        return buildMetadata(fileKey, file.getOriginalFilename(), file.getContentType(), file.getSize());
    }

    @Override
    public FileMetadata upload(InputStream inputStream, String filename, String path) throws IOException {
        String newFilename = generateFilename(filename);
        String fileKey = buildPath(path, newFilename);

        ObjectMetadata metadata = new ObjectMetadata();
        ossClient.putObject(properties.getOss().getBucket(), fileKey, inputStream, metadata);

        log.info("File uploaded to OSS: {}", fileKey);

        return buildMetadata(fileKey, filename, null, null);
    }

    @Override
    public InputStream download(String fileKey) throws IOException {
        OSSObject object = ossClient.getObject(properties.getOss().getBucket(), fileKey);
        return object.getObjectContent();
    }

    @Override
    public boolean delete(String fileKey) {
        try {
            ossClient.deleteObject(properties.getOss().getBucket(), fileKey);
            log.info("File deleted from OSS: {}", fileKey);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete file from OSS: {}", fileKey, e);
            return false;
        }
    }

    @Override
    public boolean exists(String fileKey) {
        return ossClient.doesObjectExist(properties.getOss().getBucket(), fileKey);
    }

    @Override
    public FileMetadata getFileInfo(String fileKey) {
        try {
            ObjectMetadata metadata = ossClient.getObjectMetadata(properties.getOss().getBucket(), fileKey);
            FileMetadata fileMetadata = new FileMetadata();
            fileMetadata.setFileKey(fileKey);
            fileMetadata.setSize(metadata.getContentLength());
            fileMetadata.setContentType(metadata.getContentType());
            fileMetadata.setUploadTime(LocalDateTime.now());
            return fileMetadata;
        } catch (Exception e) {
            log.error("Failed to get file info from OSS: {}", fileKey, e);
            return null;
        }
    }

    @Override
    public List<FileMetadata> listFiles(String path) {
        // Implement OSS list objects logic here
        // For now, return empty list as this is less commonly used
        return List.of();
    }

    @Override
    public String getPublicUrl(String fileKey) {
        // Return public URL if bucket is public-read
        String endpoint = properties.getOss().getEndpoint();
        String bucket = properties.getOss().getBucket();
        return String.format("https://%s.%s/%s", bucket, endpoint, fileKey);
    }

    @Override
    public String getPresignedUrl(String fileKey, int expirationSeconds) {
        Date expiration = new Date(System.currentTimeMillis() + expirationSeconds * 1000L);
        URL url = ossClient.generatePresignedUrl(properties.getOss().getBucket(), fileKey, expiration);
        return url.toString();
    }

    /**
     * Generate filename based on configuration.
     */
    private String generateFilename(String originalFilename) {
        String extension = FilenameUtils.getExtension(originalFilename);
        String baseName = FilenameUtils.getBaseName(originalFilename);

        if (properties.isEnableUuidFilename()) {
            if (properties.isKeepOriginalFilename() && StringUtils.hasText(baseName)) {
                return sanitizeFilename(baseName) + "_" + java.util.UUID.randomUUID() + "." + extension;
            }
            return java.util.UUID.randomUUID() + "." + extension;
        }
        return originalFilename;
    }

    /**
     * Build path with date partitioning.
     */
    private String buildPath(String path, String filename) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(path)) {
            sb.append(path).append("/");
        }
        if (properties.isEnableDatePath()) {
            sb.append(LocalDateTime.now().format(dateFormatter)).append("/");
        }
        sb.append(filename);
        return sb.toString();
    }

    /**
     * Build file metadata.
     */
    private FileMetadata buildMetadata(String fileKey, String originalFilename, String contentType, Long size) {
        FileMetadata metadata = new FileMetadata();
        metadata.setFileKey(fileKey);
        metadata.setOriginalFilename(originalFilename);
        metadata.setExtension(FilenameUtils.getExtension(originalFilename));
        metadata.setContentType(contentType);
        metadata.setSize(size);
        metadata.setStorageType("oss");
        metadata.setBucket(properties.getOss().getBucket());
        metadata.setUploadTime(LocalDateTime.now());
        metadata.setPublicUrl(getPublicUrl(fileKey));
        return metadata;
    }

    /**
     * Sanitize filename for safe OSS usage.
     */
    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
