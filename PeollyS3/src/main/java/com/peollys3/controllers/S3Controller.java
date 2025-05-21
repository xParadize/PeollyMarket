package com.peollys3.controllers;

import com.peolly.schemaregistry.FileCategory;
import com.peollys3.services.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Tag(name = "S3 controller")
@RequestMapping("/api/v1/s3")
public class S3Controller {
    private final S3Service s3Service;

    /**
     * Handles file upload requests and delegates to the S3 service.
     *
     * @param file  the uploaded file.
     * @param email the email associated with the file.
     */
    @Operation(summary = "Upload file")
    @PostMapping("/files")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        @RequestParam("email") String email,
                                        @RequestParam("fileCategory") FileCategory fileCategory) {
        try {
            s3Service.uploadFile(file, email, fileCategory);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_GATEWAY);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
