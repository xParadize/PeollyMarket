package com.example.peollys3.services;

import com.example.peollys3.enums.FileExtension;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FileUtils {
    /**
     * Generates a unique filename based on the original file name.
     *
     * @param file the uploaded file.
     * @return the generated file name.
     */
    public String generateFileName(MultipartFile file) {
        String extension = getFileExtension(file);
        long time = System.currentTimeMillis();
        String code = UUID.randomUUID().toString();

        return String.format("%s_%s.%s", code, time, extension);
    }

    /**
     * Extracts the file extension from the original file name.
     *
     * @param file the uploaded file.
     * @return the file extension.
     */
    private String getFileExtension(MultipartFile file) {
        return file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
    }

    /**
     * Validates and determines the file extension.
     *
     * @param fileName the name of the file.
     * @return the corresponding FileExtension enum value.
     */
    public FileExtension validateFileExtension(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toUpperCase();
        try {
            return FileExtension.valueOf(extension);
        } catch (IllegalArgumentException e) {
            return FileExtension.UNDEFINED;
        }
    }
}
