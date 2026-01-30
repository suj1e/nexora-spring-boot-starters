package com.nexora.storage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link FileUploadHelper}.
 */
@DisplayName("FileUploadHelper Tests")
@ExtendWith(MockitoExtension.class)
class FileUploadHelperTest {

    @Mock
    private MultipartFile multipartFile;

    private FileStorageProperties properties;
    private FileUploadHelper helper;

    @BeforeEach
    void setUp() {
        properties = new FileStorageProperties();
        helper = new FileUploadHelper(properties);
    }

    @Test
    @DisplayName("ValidateFile should return error for null file")
    void testValidateNullFile() {
        FileUploadHelper.ValidationError error = helper.validateFile(null);

        assertAll("Null file validation",
            () -> assertNotNull(error),
            () -> assertEquals("file.empty", error.getCode()),
            () -> assertEquals("File is empty", error.getMessage())
        );
    }

    @Test
    @DisplayName("ValidateFile should return error for empty file")
    void testValidateEmptyFile() {
        when(multipartFile.isEmpty()).thenReturn(true);

        FileUploadHelper.ValidationError error = helper.validateFile(multipartFile);

        assertAll("Empty file validation",
            () -> assertNotNull(error),
            () -> assertEquals("file.empty", error.getCode()),
            () -> assertEquals("File is empty", error.getMessage())
        );
    }

    @Test
    @DisplayName("ValidateFile should check file size")
    void testValidateFileSize() {
        properties.setMaxFileSize(new FileStorageProperties.DataSize(1, FileStorageProperties.DataSizeUnit.MB));

        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(2_000_000L); // 2MB

        FileUploadHelper.ValidationError error = helper.validateFile(multipartFile);

        assertAll("File size validation",
            () -> assertNotNull(error),
            () -> assertEquals("file.too.large", error.getCode()),
            () -> assertTrue(error.getMessage().contains("exceeds limit"))
        );
    }

    @Test
    @DisplayName("ValidateFile should check file extension")
    void testValidateFileExtension() {
        properties.setAllowedExtensions("jpg,png,pdf");

        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getOriginalFilename()).thenReturn("test.txt");

        FileUploadHelper.ValidationError error = helper.validateFile(multipartFile);

        assertAll("File extension validation",
            () -> assertNotNull(error),
            () -> assertEquals("file.type.not.allowed", error.getCode()),
            () -> assertTrue(error.getMessage().contains("not allowed"))
        );
    }

    @Test
    @DisplayName("ValidateFile should pass for valid file")
    void testValidateValidFile() {
        properties.setAllowedExtensions("jpg,png,pdf");

        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getOriginalFilename()).thenReturn("test.jpg");

        FileUploadHelper.ValidationError error = helper.validateFile(multipartFile);

        assertNull(error, "Valid file should pass validation");
    }

    @Test
    @DisplayName("ValidateFile should pass when no size limit set")
    void testValidateWithoutSizeLimit() {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(10_000_000L); // 10MB

        FileUploadHelper.ValidationError error = helper.validateFile(multipartFile);

        assertNull(error, "File should pass when no size limit is set");
    }

    @Test
    @DisplayName("IsImage should return true for image files")
    void testIsImage() {
        when(multipartFile.getOriginalFilename()).thenReturn("photo.jpg");

        assertTrue(helper.isImage(multipartFile));
    }

    @Test
    @DisplayName("IsImage should be case-insensitive")
    void testIsImageCaseInsensitive() {
        when(multipartFile.getOriginalFilename()).thenReturn("photo.JPG");

        assertTrue(helper.isImage(multipartFile));
    }

    @Test
    @DisplayName("IsImage should return false for non-image files")
    void testIsNotImage() {
        when(multipartFile.getOriginalFilename()).thenReturn("document.pdf");

        assertFalse(helper.isImage(multipartFile));
    }

    @Test
    @DisplayName("IsVideo should return true for video files")
    void testIsVideo() {
        when(multipartFile.getOriginalFilename()).thenReturn("movie.mp4");

        assertTrue(helper.isVideo(multipartFile));
    }

    @Test
    @DisplayName("IsVideo should return false for non-video files")
    void testIsNotVideo() {
        when(multipartFile.getOriginalFilename()).thenReturn("photo.jpg");

        assertFalse(helper.isVideo(multipartFile));
    }

    @Test
    @DisplayName("IsDocument should return true for document files")
    void testIsDocument() {
        when(multipartFile.getOriginalFilename()).thenReturn("report.pdf");

        assertTrue(helper.isDocument(multipartFile));
    }

    @Test
    @DisplayName("IsDocument should return false for non-document files")
    void testIsNotDocument() {
        when(multipartFile.getOriginalFilename()).thenReturn("photo.jpg");

        assertFalse(helper.isDocument(multipartFile));
    }

    @Test
    @DisplayName("GetExtension should extract extension correctly")
    void testGetExtension() {
        assertAll("Extension extraction",
            () -> assertEquals("jpg", helper.getExtension("photo.jpg")),
            () -> assertEquals("png", helper.getExtension("image.png")),
            () -> assertEquals("txt", helper.getExtension("document.txt")),
            () -> assertEquals("PDF", helper.getExtension("file.PDF")) // case preserved
        );
    }

    @Test
    @DisplayName("GetExtension should handle edge cases")
    void testGetExtensionEdgeCases() {
        assertAll("Extension edge cases",
            () -> assertNull(helper.getExtension("noextension")),
            () -> assertNull(helper.getExtension("")),
            () -> assertNull(helper.getExtension(null)),
            () -> assertNull(helper.getExtension(".")),
            () -> assertNull(helper.getExtension(".hidden"))
        );
    }

    @Test
    @DisplayName("FormatSize should format bytes correctly")
    void testFormatSize() {
        assertAll("Size formatting",
            () -> assertEquals("512 B", helper.formatSize(512)),
            () -> assertEquals("1.00 KB", helper.formatSize(1024)),
            () -> assertEquals("1.50 KB", helper.formatSize(1536)),
            () -> assertEquals("1.00 MB", helper.formatSize(1024 * 1024)),
            () -> assertEquals("1.00 GB", helper.formatSize(1024 * 1024 * 1024))
        );
    }

    @Test
    @DisplayName("GenerateSafeFilename should replace special characters")
    void testGenerateSafeFilename() {
        assertAll("Safe filename generation",
            () -> assertEquals("test_file.jpg", helper.generateSafeFilename("test/file.jpg")),
            () -> assertEquals("my_document.pdf", helper.generateSafeFilename("my?document.pdf")),
            () -> assertEquals("file_name_1.txt", helper.generateSafeFilename("file:name*1.txt")),
            () -> assertEquals("normal-name.jpg", helper.generateSafeFilename("normal-name.jpg"))
        );
    }

    @Test
    @DisplayName("GenerateSafeFilename should handle edge cases")
    void testGenerateSafeFilenameEdgeCases() {
        assertAll("Safe filename edge cases",
            () -> assertEquals("unknown", helper.generateSafeFilename("")),
            () -> assertEquals("unknown", helper.generateSafeFilename(null)),
            () -> assertEquals("_____", helper.generateSafeFilename("!!!!!"))
        );
    }

    @Test
    @DisplayName("ValidationError should store code and message")
    void testValidationError() {
        FileUploadHelper.ValidationError error =
            new FileUploadHelper.ValidationError("test.code", "Test error message");

        assertAll("Validation error",
            () -> assertEquals("test.code", error.getCode()),
            () -> assertEquals("Test error message", error.getMessage())
        );
    }
}
