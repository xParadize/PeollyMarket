package com.peollys3.services;

import com.peolly.schemaregistry.FileCategory;
import com.peollys3.config.MinioProperties;
import com.peollys3.entities.FileLink;
import com.peollys3.enums.FileStatus;
import com.peollys3.exceptions.FileUploadException;
import com.peollys3.kafka.S3KafkaProducer;
import com.peollys3.repositories.FileLinkRepository;
import io.minio.*;
import io.minio.http.Method;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final FileLinkRepository fileLinkRepository;
    private final FileUtils fileUtils;
    private final S3KafkaProducer s3KafkaProducer;

    /**
     * Uploads a file to MinIO and saves its metadata.
     *
     * @param file  the uploaded file.
     * @param email the associated email.
     */
    @Transactional
    public void uploadFile(MultipartFile file, String email, FileCategory fileCategory) {
        try {
            createMinioBucket();
        } catch (Exception e) {
            throw new FileUploadException("File upload failed: " + e.getMessage());
        }
        if (file.isEmpty()) {
            throw new FileUploadException("File upload failed: file must have name");
        }

        String fileName = fileUtils.generateFileName(file);
        InputStream inputStream;
        try {
            inputStream = file.getInputStream();
        } catch (Exception e) {
            throw new FileUploadException("File upload failed: " + e.getMessage());
        }

        String s3Key = fileCategory.name().toLowerCase() + "/" + fileName;
        saveFileToS3(inputStream, s3Key);

        String shareLink = generateShareLink(s3Key);
        s3KafkaProducer.sendDownloadLinkToEmail(shareLink, email, fileCategory);
    }

    /**
     * Ensures that the MinIO bucket exists before uploading.
     *
     * @throws Exception if bucket creation fails.
     */
    @SneakyThrows
    private void createMinioBucket() {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(minioProperties.getBucket())
                .build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .build());
        }
    }

    /**
     * Uploads the file to the MinIO storage.
     *
     * @param inputStream the file input stream.
     * @param fileName    the name of the file.
     */
    @SneakyThrows
    private void saveFileToS3(InputStream inputStream, String fileName) {
        minioClient.putObject(PutObjectArgs.builder()
                .stream(inputStream, inputStream.available(), -1)
                .bucket(minioProperties.getBucket())
                .object(fileName)
                .build());
    }

    /**
     * Generates a presigned URL for accessing the uploaded file.
     *
     * @param fileName the name of the file.
     * @return the generated share link.
     */
    @SneakyThrows
    private String generateShareLink(String fileName) {
        String shareLink = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(minioProperties.getBucket())
                        .object(fileName)
                        .expiry(7, TimeUnit.DAYS)
                        .build());
        return shareLink;
    }

    /**
     * Saves the file link metadata to the repository.
     *
     * @param fileName the name of the uploaded file.
     */
    private void saveLink(String fileName) {
        FileLink fileLink = FileLink.builder()
                .fileUrl(generateShareLink(fileName))
                .fileName(fileName)
                .bucketName(minioProperties.getBucket())
                .uploadDate(LocalDateTime.now())
                .expiryDate(LocalDateTime.now().plusDays(7))
                .status(FileStatus.ACTIVE)
                .extension(fileUtils.validateFileExtension(fileName))
                .build();
        fileLinkRepository.save(fileLink);
    }
}
