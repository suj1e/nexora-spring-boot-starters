package com.nexora.storage.local;

import com.nexora.storage.FileMetadata;
import com.nexora.storage.FileStorageProperties;
import com.nexora.storage.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

/**
 * Local file system storage implementation.
 *
 * <p>Stores files on the local file system with date-based path partitioning.
 *
 * @author sujie
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "nexora.file-storage", name = "type", havingValue = "local")
@EnableConfigurationProperties(FileStorageProperties.class)
public class LocalStorage implements FileStorageService {

    private final FileStorageProperties properties;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    public LocalStorage(FileStorageProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(properties.getUploadPath());
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created upload directory: {}", properties.getUploadPath());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create upload directory", e);
        }
    }

    @Override
    public FileMetadata upload(MultipartFile file, String path) throws IOException {
        String filename = generateFilename(file.getOriginalFilename());
        String relativePath = buildPath(path, filename);
        String fullPath = properties.getUploadPath() + "/" + relativePath;

        Path targetPath = Paths.get(fullPath);
        Files.createDirectories(targetPath.getParent());
        file.transferTo(targetPath.toFile());

        log.info("File uploaded to: {}", fullPath);

        return buildMetadata(relativePath, file.getOriginalFilename(), file.getContentType(), file.getSize());
    }

    @Override
    public FileMetadata upload(InputStream inputStream, String filename, String path) throws IOException {
        String newFilename = generateFilename(filename);
        String relativePath = buildPath(path, newFilename);
        String fullPath = properties.getUploadPath() + "/" + relativePath;

        Path targetPath = Paths.get(fullPath);
        Files.createDirectories(targetPath.getParent());
        Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);

        log.info("File uploaded to: {}", fullPath);

        return buildMetadata(relativePath, filename, null, new File(fullPath).length());
    }

    @Override
    public InputStream download(String fileKey) throws IOException {
        String fullPath = properties.getUploadPath() + "/" + fileKey;
        Path path = Paths.get(fullPath);
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + fileKey);
        }
        return new FileInputStream(path.toFile());
    }

    @Override
    public boolean delete(String fileKey) {
        String fullPath = properties.getUploadPath() + "/" + fileKey;
        Path path = Paths.get(fullPath);
        try {
            boolean deleted = Files.deleteIfExists(path);
            if (deleted) {
                log.info("File deleted: {}", fileKey);
            }
            return deleted;
        } catch (IOException e) {
            log.error("Failed to delete file: {}", fileKey, e);
            return false;
        }
    }

    @Override
    public boolean exists(String fileKey) {
        String fullPath = properties.getUploadPath() + "/" + fileKey;
        return Files.exists(Paths.get(fullPath));
    }

    @Override
    public FileMetadata getFileInfo(String fileKey) {
        String fullPath = properties.getUploadPath() + "/" + fileKey;
        Path path = Paths.get(fullPath);
        if (!Files.exists(path)) {
            return null;
        }
        try {
            FileMetadata metadata = new FileMetadata();
            metadata.setFileKey(fileKey);
            metadata.setSize(Files.size(path));
            metadata.setUploadTime(LocalDateTime.now());
            return metadata;
        } catch (IOException e) {
            log.error("Failed to get file info: {}", fileKey, e);
            return null;
        }
    }

    @Override
    public List<FileMetadata> listFiles(String path) {
        String fullPath = properties.getUploadPath() + "/" + path;
        List<FileMetadata> files = new ArrayList<>();
        try {
            File dir = new File(fullPath);
            if (dir.exists() && dir.isDirectory()) {
                File[] fileArray = dir.listFiles();
                if (fileArray != null) {
                    for (File file : fileArray) {
                        if (file.isFile()) {
                            FileMetadata metadata = new FileMetadata();
                            metadata.setFileKey(path + "/" + file.getName());
                            metadata.setSize(file.length());
                            files.add(metadata);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to list files: {}", path, e);
        }
        return files;
    }

    @Override
    public String getPublicUrl(String fileKey) {
        if (StringUtils.hasText(properties.getBaseUrl())) {
            return properties.getBaseUrl() + "/" + fileKey;
        }
        return null;
    }

    @Override
    public String getPresignedUrl(String fileKey, int expirationSeconds) {
        // Local storage doesn't support presigned URLs
        return getPublicUrl(fileKey);
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
    private FileMetadata buildMetadata(String fileKey, String originalFilename, String contentType, long size) {
        FileMetadata metadata = new FileMetadata();
        metadata.setFileKey(fileKey);
        metadata.setOriginalFilename(originalFilename);
        metadata.setExtension(FilenameUtils.getExtension(originalFilename));
        metadata.setContentType(contentType);
        metadata.setSize(size);
        metadata.setStorageType("local");
        metadata.setUploadTime(LocalDateTime.now());

        if (properties.isEnableHash()) {
            try {
                String fullPath = properties.getUploadPath() + "/" + fileKey;
                metadata.setHash(calculateHash(fullPath));
            } catch (Exception e) {
                log.warn("Failed to calculate file hash", e);
            }
        }

        metadata.setPublicUrl(getPublicUrl(fileKey));

        return metadata;
    }

    /**
     * Calculate file hash.
     */
    private String calculateHash(String filePath) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest;
        if (properties.getHashAlgorithm() == FileStorageProperties.HashAlgorithm.MD5) {
            digest = MessageDigest.getInstance("MD5");
        } else if (properties.getHashAlgorithm() == FileStorageProperties.HashAlgorithm.SHA512) {
            digest = MessageDigest.getInstance("SHA-512");
        } else {
            digest = MessageDigest.getInstance("SHA-256");
        }

        byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
        byte[] hashBytes = digest.digest(fileBytes);
        return HexFormat.of().formatHex(hashBytes);
    }

    /**
     * Sanitize filename for safe file system usage.
     */
    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
