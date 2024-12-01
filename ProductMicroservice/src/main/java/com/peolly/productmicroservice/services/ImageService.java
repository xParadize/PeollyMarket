package com.peolly.productmicroservice.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageService {

    @Transactional(readOnly = true)
    public Boolean isFileEmpty(MultipartFile file) {
        return file.isEmpty();
    }
}
