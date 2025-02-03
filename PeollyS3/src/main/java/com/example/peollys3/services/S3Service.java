package com.example.peollys3.services;

import com.example.peollys3.config.MinioProperties;
import com.example.peollys3.entities.FileLink;
import com.example.peollys3.enums.FileStatus;
import com.example.peollys3.exceptions.FileUploadException;
import com.example.peollys3.kafka.S3KafkaProducer;
import com.example.peollys3.repositories.FileLinkRepository;
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

    @Transactional
    public void uploadFile(MultipartFile file, String email) {
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
        saveFileToS3(inputStream, fileName);
        saveLink(fileName);
        s3KafkaProducer.sendFileLinkToEmail(generateShareLink(fileName), email);
    }

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

    @SneakyThrows
    private void saveFileToS3(InputStream inputStream, String fileName) {
        minioClient.putObject(PutObjectArgs.builder()
                .stream(inputStream, inputStream.available(), -1)
                .bucket(minioProperties.getBucket())
                .object(fileName)
                .build());
    }

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
