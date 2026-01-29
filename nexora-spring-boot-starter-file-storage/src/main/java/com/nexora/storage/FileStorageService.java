package com.nexora.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * File storage service abstraction.
 *
 * <p>Provides a unified interface for storing files across different backends
 * (local file system, OSS, S3, MinIO, etc.).
 *
 * <p>Usage example:
 * <pre>
 * &#64;Autowired
 * private FileStorageService fileStorageService;
 *
 * // Upload file
 * FileMetadata metadata = fileStorageService.upload(file, "photos/");
 *
 * // Download file
 * InputStream stream = fileStorageService.download("photos/abc.jpg");
 *
 * // Delete file
 * fileStorageService.delete("photos/abc.jpg");
 *
 * // Get file info
 * FileMetadata info = fileStorageService.getFileInfo("photos/abc.jpg");
 * </pre>
 *
 * @author sujie
 */
public interface FileStorageService {

    /**
     * Upload a file to the storage backend.
     *
     * @param file the file to upload
     * @param path the path where to store the file (e.g., "photos/", "documents/")
     * @return the file metadata
     * @throws IOException if upload fails
     */
    FileMetadata upload(MultipartFile file, String path) throws IOException;

    /**
     * Upload a file from input stream to the storage backend.
     *
     * @param inputStream the input stream to read from
     * @param filename    the original filename
     * @param path        the path where to store the file
     * @return the file metadata
     * @throws IOException if upload fails
     */
    FileMetadata upload(InputStream inputStream, String filename, String path) throws IOException;

    /**
     * Download a file from the storage backend.
     *
     * @param fileKey the file key (path)
     * @return the input stream
     * @throws IOException if download fails
     */
    InputStream download(String fileKey) throws IOException;

    /**
     * Delete a file from the storage backend.
     *
     * @param fileKey the file key (path)
     * @return true if deleted, false otherwise
     */
    boolean delete(String fileKey);

    /**
     * Check if a file exists.
     *
     * @param fileKey the file key (path)
     * @return true if exists, false otherwise
     */
    boolean exists(String fileKey);

    /**
     * Get file metadata.
     *
     * @param fileKey the file key (path)
     * @return the file metadata, or null if not exists
     */
    FileMetadata getFileInfo(String fileKey);

    /**
     * List files in a directory.
     *
     * @param path the directory path
     * @return list of file metadata
     */
    List<FileMetadata> listFiles(String path);

    /**
     * Get a public URL for the file (if supported).
     *
     * @param fileKey the file key (path)
     * @return the public URL, or null if not supported
     */
    String getPublicUrl(String fileKey);

    /**
     * Generate a presigned URL for temporary access (if supported).
     *
     * @param fileKey the file key (path)
     * @param expirationSeconds the expiration time in seconds
     * @return the presigned URL, or null if not supported
     */
    String getPresignedUrl(String fileKey, int expirationSeconds);
}
