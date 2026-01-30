package com.nexora.storage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link FileMetadata}.
 */
@DisplayName("FileMetadata Tests")
class FileMetadataTest {

    @Test
    @DisplayName("All fields should be settable and gettable")
    void testAllFieldsSettableAndGettable() {
        FileMetadata metadata = new FileMetadata();
        LocalDateTime now = LocalDateTime.now();
        Map<String, String> extraMetadata = new HashMap<>();
        extraMetadata.put("width", "1920");
        extraMetadata.put("height", "1080");

        metadata.setFileKey("uploads/2024/01/test-file.jpg");
        metadata.setOriginalFilename("test-file.jpg");
        metadata.setExtension("jpg");
        metadata.setContentType("image/jpeg");
        metadata.setSize(1024000L);
        metadata.setHash("abc123def456");
        metadata.setStorageType("oss");
        metadata.setBucket("my-bucket");
        metadata.setPublicUrl("https://cdn.example.com/test-file.jpg");
        metadata.setUploadTime(now);
        metadata.setMetadata(extraMetadata);

        assertAll("All fields",
            () -> assertEquals("uploads/2024/01/test-file.jpg", metadata.getFileKey()),
            () -> assertEquals("test-file.jpg", metadata.getOriginalFilename()),
            () -> assertEquals("jpg", metadata.getExtension()),
            () -> assertEquals("image/jpeg", metadata.getContentType()),
            () -> assertEquals(1024000L, metadata.getSize()),
            () -> assertEquals("abc123def456", metadata.getHash()),
            () -> assertEquals("oss", metadata.getStorageType()),
            () -> assertEquals("my-bucket", metadata.getBucket()),
            () -> assertEquals("https://cdn.example.com/test-file.jpg", metadata.getPublicUrl()),
            () -> assertEquals(now, metadata.getUploadTime()),
            () -> assertEquals(extraMetadata, metadata.getMetadata())
        );
    }

    @Test
    @DisplayName("IsImage should return true for image extensions")
    void testIsImageForImageFiles() {
        FileMetadata jpg = new FileMetadata();
        jpg.setExtension("jpg");

        FileMetadata png = new FileMetadata();
        png.setExtension("png");

        FileMetadata jpeg = new FileMetadata();
        jpeg.setExtension("jpeg");

        FileMetadata gif = new FileMetadata();
        gif.setExtension("gif");

        FileMetadata bmp = new FileMetadata();
        bmp.setExtension("bmp");

        FileMetadata webp = new FileMetadata();
        webp.setExtension("webp");

        assertAll("Image file detection",
            () -> assertTrue(jpg.isImage()),
            () -> assertTrue(png.isImage()),
            () -> assertTrue(jpeg.isImage()),
            () -> assertTrue(gif.isImage()),
            () -> assertTrue(bmp.isImage()),
            () -> assertTrue(webp.isImage())
        );
    }

    @Test
    @DisplayName("IsImage should return false for non-image extensions")
    void testIsImageForNonImageFiles() {
        FileMetadata pdf = new FileMetadata();
        pdf.setExtension("pdf");

        FileMetadata mp4 = new FileMetadata();
        mp4.setExtension("mp4");

        FileMetadata txt = new FileMetadata();
        txt.setExtension("txt");

        FileMetadata nullExt = new FileMetadata();
        nullExt.setExtension(null);

        assertAll("Non-image file detection",
            () -> assertFalse(pdf.isImage()),
            () -> assertFalse(mp4.isImage()),
            () -> assertFalse(txt.isImage()),
            () -> assertFalse(nullExt.isImage())
        );
    }

    @Test
    @DisplayName("IsImage should be case-insensitive")
    void testIsImageCaseInsensitive() {
        FileMetadata uppercase = new FileMetadata();
        uppercase.setExtension("JPG");

        FileMetadata mixedCase = new FileMetadata();
        mixedCase.setExtension("PnG");

        assertAll("Case insensitive image detection",
            () -> assertTrue(uppercase.isImage()),
            () -> assertTrue(mixedCase.isImage())
        );
    }

    @Test
    @DisplayName("IsVideo should return true for video extensions")
    void testIsVideoForVideoFiles() {
        FileMetadata mp4 = new FileMetadata();
        mp4.setExtension("mp4");

        FileMetadata avi = new FileMetadata();
        avi.setExtension("avi");

        FileMetadata mov = new FileMetadata();
        mov.setExtension("mov");

        FileMetadata mkv = new FileMetadata();
        mkv.setExtension("mkv");

        assertAll("Video file detection",
            () -> assertTrue(mp4.isVideo()),
            () -> assertTrue(avi.isVideo()),
            () -> assertTrue(mov.isVideo()),
            () -> assertTrue(mkv.isVideo())
        );
    }

    @Test
    @DisplayName("IsVideo should return false for non-video extensions")
    void testIsVideoForNonVideoFiles() {
        FileMetadata jpg = new FileMetadata();
        jpg.setExtension("jpg");

        FileMetadata pdf = new FileMetadata();
        pdf.setExtension("pdf");

        FileMetadata nullExt = new FileMetadata();
        nullExt.setExtension(null);

        assertAll("Non-video file detection",
            () -> assertFalse(jpg.isVideo()),
            () -> assertFalse(pdf.isVideo()),
            () -> assertFalse(nullExt.isVideo())
        );
    }

    @Test
    @DisplayName("IsDocument should return true for document extensions")
    void testIsDocumentForDocumentFiles() {
        FileMetadata pdf = new FileMetadata();
        pdf.setExtension("pdf");

        FileMetadata docx = new FileMetadata();
        docx.setExtension("docx");

        FileMetadata xlsx = new FileMetadata();
        xlsx.setExtension("xlsx");

        FileMetadata txt = new FileMetadata();
        txt.setExtension("txt");

        assertAll("Document file detection",
            () -> assertTrue(pdf.isDocument()),
            () -> assertTrue(docx.isDocument()),
            () -> assertTrue(xlsx.isDocument()),
            () -> assertTrue(txt.isDocument())
        );
    }

    @Test
    @DisplayName("IsDocument should return false for non-document extensions")
    void testIsDocumentForNonDocumentFiles() {
        FileMetadata jpg = new FileMetadata();
        jpg.setExtension("jpg");

        FileMetadata mp4 = new FileMetadata();
        mp4.setExtension("mp4");

        FileMetadata nullExt = new FileMetadata();
        nullExt.setExtension(null);

        assertAll("Non-document file detection",
            () -> assertFalse(jpg.isDocument()),
            () -> assertFalse(mp4.isDocument()),
            () -> assertFalse(nullExt.isDocument())
        );
    }

    @Test
    @DisplayName("GetFormattedSize should format bytes correctly")
    void testGetFormattedSize() {
        FileMetadata bytes = new FileMetadata();
        bytes.setSize(500L);

        FileMetadata kb = new FileMetadata();
        kb.setSize(1024L);

        FileMetadata mb = new FileMetadata();
        mb.setSize(1024L * 1024);

        FileMetadata gb = new FileMetadata();
        gb.setSize(1024L * 1024 * 1024);

        assertAll("Size formatting",
            () -> assertEquals("500 B", bytes.getFormattedSize()),
            () -> assertTrue(kb.getFormattedSize().endsWith("KB")),
            () -> assertTrue(mb.getFormattedSize().endsWith("MB")),
            () -> assertTrue(gb.getFormattedSize().endsWith("GB"))
        );
    }

    @Test
    @DisplayName("GetFormattedSize should return Unknown for null size")
    void testGetFormattedSizeForNullSize() {
        FileMetadata metadata = new FileMetadata();
        metadata.setSize(null);

        assertEquals("Unknown", metadata.getFormattedSize());
    }

    @Test
    @DisplayName("GetFormattedSize should handle decimal precision")
    void testGetFormattedSizeDecimalPrecision() {
        FileMetadata size1 = new FileMetadata();
        size1.setSize(1536L); // 1.5 KB

        FileMetadata size2 = new FileMetadata();
        size2.setSize(5242880L); // 5 MB

        assertAll("Decimal precision",
            () -> assertEquals("1.50 KB", size1.getFormattedSize()),
            () -> assertEquals("5.00 MB", size2.getFormattedSize())
        );
    }
}
