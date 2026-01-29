package com.nexora.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Helper class for file upload validation and utilities.
 *
 * <p>Usage example:
 * <pre>
 * &#64;Autowired
 * private FileStorageService fileStorageService;
 *
 * &#64;PostMapping("/upload")
 * public Result&lt;FileMetadata&gt; upload(&#64;RequestParam("file") MultipartFile file) {
 *     // Validate file
 *     FileUploadHelper.ValidationError error = fileUploadHelper.validateFile(file);
 *     if (error != null) {
 *         return Result.fail(error.getMessage());
 *     }
 *
 *     // Upload file
 *     FileMetadata metadata = fileStorageService.upload(file, "uploads/");
 *     return Result.ok(metadata);
 * }
 * </pre>
 *
 * @author sujie
 */
@Slf4j
public class FileUploadHelper {

    private final FileStorageProperties properties;

    public FileUploadHelper(FileStorageProperties properties) {
        this.properties = properties;
    }

    /**
     * Validate file before upload.
     *
     * @param file the file to validate
     * @return validation error, or null if valid
     */
    public ValidationError validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return new ValidationError("file.empty", "File is empty");
        }

        // Check file size
        if (properties.getMaxFileSize() != null) {
            long maxBytes = properties.getMaxFileSize().toBytes();
            if (file.getSize() > maxBytes) {
                return new ValidationError("file.too.large",
                        String.format("File size exceeds limit: %s > %s",
                                formatSize(file.getSize()),
                                formatSize(maxBytes)));
            }
        }

        // Check file extension
        if (StringUtils.hasText(properties.getAllowedExtensions())) {
            String extension = getExtension(file.getOriginalFilename());
            Set<String> allowedExtensions = new HashSet<>(
                    Arrays.asList(properties.getAllowedExtensions().split(","))
            );

            if (!allowedExtensions.contains(extension.toLowerCase())) {
                return new ValidationError("file.type.not.allowed",
                        String.format("File type not allowed: %s. Allowed types: %s",
                                extension, properties.getAllowedExtensions()));
            }
        }

        return null;
    }

    /**
     * Check if file is an image.
     *
     * @param file the file to check
     * @return true if image
     */
    public boolean isImage(MultipartFile file) {
        String extension = getExtension(file.getOriginalFilename());
        return extension != null && (
                extension.equalsIgnoreCase("jpg") ||
                        extension.equalsIgnoreCase("jpeg") ||
                        extension.equalsIgnoreCase("png") ||
                        extension.equalsIgnoreCase("gif") ||
                        extension.equalsIgnoreCase("bmp") ||
                        extension.equalsIgnoreCase("webp")
        );
    }

    /**
     * Check if file is a video.
     *
     * @param file the file to check
     * @return true if video
     */
    public boolean isVideo(MultipartFile file) {
        String extension = getExtension(file.getOriginalFilename());
        return extension != null && (
                extension.equalsIgnoreCase("mp4") ||
                        extension.equalsIgnoreCase("avi") ||
                        extension.equalsIgnoreCase("mov") ||
                        extension.equalsIgnoreCase("wmv") ||
                        extension.equalsIgnoreCase("flv") ||
                        extension.equalsIgnoreCase("mkv")
        );
    }

    /**
     * Check if file is a document.
     *
     * @param file the file to check
     * @return true if document
     */
    public boolean isDocument(MultipartFile file) {
        String extension = getExtension(file.getOriginalFilename());
        return extension != null && (
                extension.equalsIgnoreCase("pdf") ||
                        extension.equalsIgnoreCase("doc") ||
                        extension.equalsIgnoreCase("docx") ||
                        extension.equalsIgnoreCase("xls") ||
                        extension.equalsIgnoreCase("xlsx") ||
                        extension.equalsIgnoreCase("ppt") ||
                        extension.equalsIgnoreCase("pptx") ||
                        extension.equalsIgnoreCase("txt")
        );
    }

    /**
     * Get file extension from filename.
     *
     * @param filename the filename
     * @return the extension, or null if not found
     */
    public String getExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return null;
        }
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1);
        }
        return null;
    }

    /**
     * Format file size for display.
     *
     * @param bytes the file size in bytes
     * @return formatted size string
     */
    public String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * Generate safe filename.
     *
     * @param filename the original filename
     * @return safe filename
     */
    public String generateSafeFilename(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "unknown";
        }
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    /**
     * Validation error class.
     */
    public static class ValidationError {
        private final String code;
        private final String message;

        public ValidationError(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
