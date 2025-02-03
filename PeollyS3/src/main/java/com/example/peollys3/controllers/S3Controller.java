package com.example.peollys3.controllers;

import com.example.peollys3.services.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/s3")
public class S3Controller {
    private final S3Service s3Service;

    @PostMapping("/upload")
    public void uploadFile(@RequestParam("file") MultipartFile file,
                           @RequestParam("email") String email) {
        s3Service.uploadFile(file, email);
    }
}
