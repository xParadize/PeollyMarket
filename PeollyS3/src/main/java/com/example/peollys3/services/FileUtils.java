package com.example.peollys3.services;

import com.example.peollys3.enums.FileExtension;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FileUtils {
    public String generateFileName(MultipartFile file) {
        String extension = getFileExtension(file);
        long time = System.currentTimeMillis();
        String code = UUID.randomUUID().toString();

        return String.format("%s_%s.%s", code, time, extension);
    }

    private String getFileExtension(MultipartFile file) {
        return file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
    }

    public FileExtension validateFileExtension(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toUpperCase();
        try {
            return FileExtension.valueOf(extension);
        } catch (IllegalArgumentException e) {
            return FileExtension.UNDEFINED;
        }
    }
}
