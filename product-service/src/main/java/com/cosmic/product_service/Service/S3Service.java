package com.cosmic.product_service.Service;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {
    String uploadFile(MultipartFile file, String folderName);

    String uploadBytes(byte[] data, String contentType, String folderName);

    void deleteFileByUrl(String fileUrl);
}
