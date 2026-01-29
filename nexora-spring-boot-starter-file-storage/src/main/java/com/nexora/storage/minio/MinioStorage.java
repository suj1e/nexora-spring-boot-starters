package com.nexora.storage.minio;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.MinioException;
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
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * MinIO storage implementation.
 *
 * <p>Requires io.minio:minio dependency.
 *
 * @author sujie
 */
@Slf4j
@Service
@ConditionalOnClass(MinioClient.class)
@ConditionalOnProperty(prefix = "nexora.file-storage", name = "type", havingValue = "minio")
@EnableConfigurationProperties(FileStorageProperties.class)
public class MinioStorage implements FileStorageService {

    private final FileStorageProperties properties;
    private MinioClient minioClient;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    public MinioStorage(FileStorageProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        FileStorageProperties.Minio minioConfig = properties.getMinio();
        if (minioConfig == null || !StringUtils.hasText(minioConfig.getEndpoint()) ||
                !StringUtils.hasText(minioConfig.getAccessKey()) ||
                !StringUtils.hasText(minioConfig.getSecretKey()) ||
                !StringUtils.hasText(minioConfig.getBucket())) {
            throw new IllegalArgumentException("MinIO configuration is incomplete");
        }

        minioClient = MinioClient.builder()
                .endpoint(minioConfig.getEndpoint())
                .credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey())
                .build();

        log.info("MinIO storage initialized with endpoint: {}, bucket: {}",
                minioConfig.getEndpoint(), minioConfig.getBucket());
    }

    @Override
    public FileMetadata upload(MultipartFile file, String path) throws IOException {
        String filename = generateFilename(file.getOriginalFilename());
        String fileKey = buildPath(path, filename);

        try {
            PutObjectArgs.Builder requestBuilder = PutObjectArgs.builder()
                    .bucket(properties.getMinio().getBucket())
                    .object(fileKey)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType());

            minioClient.putObject(requestBuilder.build());

            log.info("File uploaded to MinIO: {}", fileKey);

            return buildMetadata(fileKey, file.getOriginalFilename(), file.getContentType(), file.getSize());
        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new IOException("Failed to upload file to MinIO", e);
        }
    }

    @Override
    public FileMetadata upload(InputStream inputStream, String filename, String path) throws IOException {
        String newFilename = generateFilename(filename);
        String fileKey = buildPath(path, newFilename);

        try {
            PutObjectArgs.Builder requestBuilder = PutObjectArgs.builder()
                    .bucket(properties.getMinio().getBucket())
                    .object(fileKey)
                    .stream(inputStream, -1, 10485760); // 10MB part size

            minioClient.putObject(requestBuilder.build());

            log.info("File uploaded to MinIO: {}", fileKey);

            return buildMetadata(fileKey, filename, null, null);
        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new IOException("Failed to upload file to MinIO", e);
        }
    }

    @Override
    public InputStream download(String fileKey) throws IOException {
        try {
            GetObjectArgs request = GetObjectArgs.builder()
                    .bucket(properties.getMinio().getBucket())
                    .object(fileKey)
                    .build();
            return minioClient.getObject(request);
        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new IOException("Failed to download file from MinIO: " + fileKey, e);
        }
    }

    @Override
    public boolean delete(String fileKey) {
        try {
            RemoveObjectArgs request = RemoveObjectArgs.builder()
                    .bucket(properties.getMinio().getBucket())
                    .object(fileKey)
                    .build();
            minioClient.removeObject(request);
            log.info("File deleted from MinIO: {}", fileKey);
            return true;
        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException e) {
            log.error("Failed to delete file from MinIO: {}", fileKey, e);
            return false;
        }
    }

    @Override
    public boolean exists(String fileKey) {
        try {
            StatObjectArgs request = StatObjectArgs.builder()
                    .bucket(properties.getMinio().getBucket())
                    .object(fileKey)
                    .build();
            minioClient.statObject(request);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public FileMetadata getFileInfo(String fileKey) {
        try {
            StatObjectArgs request = StatObjectArgs.builder()
                    .bucket(properties.getMinio().getBucket())
                    .object(fileKey)
                    .build();
            var stat = minioClient.statObject(request);

            FileMetadata metadata = new FileMetadata();
            metadata.setFileKey(fileKey);
            metadata.setSize(stat.size());
            metadata.setContentType(stat.contentType());
            metadata.setUploadTime(LocalDateTime.now());
            return metadata;
        } catch (Exception e) {
            log.error("Failed to get file info from MinIO: {}", fileKey, e);
            return null;
        }
    }

    @Override
    public List<FileMetadata> listFiles(String path) {
        // Implement MinIO list objects logic here
        return List.of();
    }

    @Override
    public String getPublicUrl(String fileKey) {
        String endpoint = properties.getMinio().getEndpoint();
        String bucket = properties.getMinio().getBucket();
        return String.format("%s/%s/%s", endpoint, bucket, fileKey);
    }

    @Override
    public String getPresignedUrl(String fileKey, int expirationSeconds) {
        try {
            return minioClient.getPresignedObjectUrl(
                    io.minio.GetPresignedObjectUrlArgs.builder()
                            .method(io.minio.http.Method.GET)
                            .bucket(properties.getMinio().getBucket())
                            .object(fileKey)
                            .expiry(expirationSeconds, TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to generate presigned URL", e);
            return getPublicUrl(fileKey);
        }
    }

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

    private FileMetadata buildMetadata(String fileKey, String originalFilename, String contentType, Long size) {
        FileMetadata metadata = new FileMetadata();
        metadata.setFileKey(fileKey);
        metadata.setOriginalFilename(originalFilename);
        metadata.setExtension(FilenameUtils.getExtension(originalFilename));
        metadata.setContentType(contentType);
        metadata.setSize(size);
        metadata.setStorageType("minio");
        metadata.setBucket(properties.getMinio().getBucket());
        metadata.setUploadTime(LocalDateTime.now());
        metadata.setPublicUrl(getPublicUrl(fileKey));
        return metadata;
    }

    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
