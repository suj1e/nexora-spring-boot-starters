package com.nexora.storage;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * File metadata information.
 *
 * <p>Contains file information such as name, size, type, upload time, etc.
 *
 * @author sujie
 */
@Data
public class FileMetadata {

    /**
     * Unique file identifier (key/path).
     */
    private String fileKey;

    /**
     * Original filename.
     */
    private String originalFilename;

    /**
     * File extension.
     */
    private String extension;

    /**
     * Content type (MIME type).
     */
    private String contentType;

    /**
     * File size in bytes.
     */
    private Long size;

    /**
     * File hash (MD5/SHA-256) for deduplication.
     */
    private String hash;

    /**
     * Storage type (local, oss, s3, minio).
     */
    private String storageType;

    /**
     * Bucket name (for cloud storage).
     */
    private String bucket;

    /**
     * Public URL for accessing the file.
     */
    private String publicUrl;

    /**
     * Upload timestamp.
     */
    private LocalDateTime uploadTime;

    /**
     * Additional metadata.
     */
    private Map<String, String> metadata;

    /**
     * Check if this is an image file.
     *
     * @return true if image file
     */
    public boolean isImage() {
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
     * Check if this is a video file.
     *
     * @return true if video file
     */
    public boolean isVideo() {
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
     * Check if this is a document file.
     *
     * @return true if document file
     */
    public boolean isDocument() {
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
     * Get human-readable file size.
     *
     * @return formatted size string
     */
    public String getFormattedSize() {
        if (size == null) {
            return "Unknown";
        }
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }
}
