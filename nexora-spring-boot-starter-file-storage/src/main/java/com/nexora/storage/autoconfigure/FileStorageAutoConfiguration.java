package com.nexora.storage.autoconfigure;

import com.nexora.storage.FileStorageProperties;
import com.nexora.storage.FileStorageService;
import com.nexora.storage.FileUploadHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * File storage auto-configuration.
 *
 * <p>Registers {@link FileStorageProperties} for configuration binding.
 * The actual storage implementation (Local, OSS, S3, MinIO) is activated
 * via {@code @ConditionalOnProperty} on each implementation class.
 *
 * <p>To enable file storage, set:
 * <pre>
 * nexora.file-storage.type=local|oss|s3|minio
 * </pre>
 *
 * <p>Usage example:
 * <pre>
 * &#64;RestController
 * &#64;RequestMapping("/api/files")
 * public class FileController {
 *
 *     &#64;Autowired
 *     private FileStorageService fileStorageService;
 *
 *     &#64;PostMapping("/upload")
 *     public Result&lt;FileMetadata&gt; upload(&#64;RequestParam("file") MultipartFile file) {
 *         FileMetadata metadata = fileStorageService.upload(file, "uploads/");
 *         return Result.ok(metadata);
 *     }
 * }
 * </pre>
 *
 * @author sujie
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(FileStorageProperties.class)
public class FileStorageAutoConfiguration {

    public FileStorageAutoConfiguration() {
        log.info("File storage auto-configuration initialized");
    }

    /**
     * File upload helper bean for validation and utilities.
     */
    @Bean
    @ConditionalOnMissingBean
    public FileUploadHelper fileUploadHelper(FileStorageProperties properties) {
        return new FileUploadHelper(properties);
    }
}
