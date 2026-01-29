package com.nexora.storage.s3;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
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
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * AWS S3 storage implementation.
 *
 * <p>Requires software.amazon.awssdk:s3 dependency.
 *
 * @author sujie
 */
@Slf4j
@Service
@ConditionalOnClass(S3Client.class)
@ConditionalOnProperty(prefix = "nexora.file-storage", name = "type", havingValue = "s3")
@EnableConfigurationProperties(FileStorageProperties.class)
public class S3Storage implements FileStorageService {

    private final FileStorageProperties properties;
    private S3Client s3Client;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    public S3Storage(FileStorageProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        FileStorageProperties.S3 s3Config = properties.getS3();
        if (s3Config == null || !StringUtils.hasText(s3Config.getRegion()) ||
                !StringUtils.hasText(s3Config.getAccessKeyId()) ||
                !StringUtils.hasText(s3Config.getSecretAccessKey()) ||
                !StringUtils.hasText(s3Config.getBucket())) {
            throw new IllegalArgumentException("S3 configuration is incomplete");
        }

        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                s3Config.getAccessKeyId(),
                s3Config.getSecretAccessKey()
        );

        var builder = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(s3Config.getRegion()));

        // For S3-compatible services (like MinIO), use custom endpoint
        if (StringUtils.hasText(s3Config.getEndpoint())) {
            builder.endpointOverride(URI.create(s3Config.getEndpoint()));
        }

        s3Client = builder.build();

        log.info("S3 storage initialized with region: {}, bucket: {}",
                s3Config.getRegion(), s3Config.getBucket());
    }

    @PreDestroy
    public void destroy() {
        if (s3Client != null) {
            s3Client.close();
        }
    }

    @Override
    public FileMetadata upload(MultipartFile file, String path) throws IOException {
        String filename = generateFilename(file.getOriginalFilename());
        String fileKey = buildPath(path, filename);

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(properties.getS3().getBucket())
                    .key(fileKey)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("File uploaded to S3: {}", fileKey);

            return buildMetadata(fileKey, file.getOriginalFilename(), file.getContentType(), file.getSize());
        } catch (S3Exception e) {
            throw new IOException("Failed to upload file to S3", e);
        }
    }

    @Override
    public FileMetadata upload(InputStream inputStream, String filename, String path) throws IOException {
        String newFilename = generateFilename(filename);
        String fileKey = buildPath(path, newFilename);

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(properties.getS3().getBucket())
                    .key(fileKey)
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, -1));

            log.info("File uploaded to S3: {}", fileKey);

            return buildMetadata(fileKey, filename, null, null);
        } catch (S3Exception e) {
            throw new IOException("Failed to upload file to S3", e);
        }
    }

    @Override
    public InputStream download(String fileKey) throws IOException {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(properties.getS3().getBucket())
                    .key(fileKey)
                    .build();
            return s3Client.getObject(request);
        } catch (S3Exception e) {
            throw new IOException("Failed to download file from S3: " + fileKey, e);
        }
    }

    @Override
    public boolean delete(String fileKey) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(properties.getS3().getBucket())
                    .key(fileKey)
                    .build();
            s3Client.deleteObject(request);
            log.info("File deleted from S3: {}", fileKey);
            return true;
        } catch (S3Exception e) {
            log.error("Failed to delete file from S3: {}", fileKey, e);
            return false;
        }
    }

    @Override
    public boolean exists(String fileKey) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(properties.getS3().getBucket())
                    .key(fileKey)
                    .build();
            s3Client.headObject(request);
            return true;
        } catch (S3Exception e) {
            return false;
        }
    }

    @Override
    public FileMetadata getFileInfo(String fileKey) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(properties.getS3().getBucket())
                    .key(fileKey)
                    .build();
            var response = s3Client.headObject(request);
            FileMetadata metadata = new FileMetadata();
            metadata.setFileKey(fileKey);
            metadata.setSize(response.contentLength());
            metadata.setContentType(response.contentType());
            metadata.setUploadTime(LocalDateTime.now());
            return metadata;
        } catch (S3Exception e) {
            log.error("Failed to get file info from S3: {}", fileKey, e);
            return null;
        }
    }

    @Override
    public List<FileMetadata> listFiles(String path) {
        // Implement S3 list objects logic here
        return List.of();
    }

    @Override
    public String getPublicUrl(String fileKey) {
        String bucket = properties.getS3().getBucket();
        String region = properties.getS3().getRegion();
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, fileKey);
    }

    @Override
    public String getPresignedUrl(String fileKey, int expirationSeconds) {
        // S3 presigned URLs require S3Presigner, returning public URL for now
        // You can implement S3Presigner if needed
        return getPublicUrl(fileKey);
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
        metadata.setStorageType("s3");
        metadata.setBucket(properties.getS3().getBucket());
        metadata.setUploadTime(LocalDateTime.now());
        metadata.setPublicUrl(getPublicUrl(fileKey));
        return metadata;
    }

    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
