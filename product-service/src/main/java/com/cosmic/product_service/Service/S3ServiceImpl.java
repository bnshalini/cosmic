package com.cosmic.product_service.Service;
import com.cosmic.product_service.exceptions.InvalidFileException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service{
    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    @Override
    public String uploadFile(MultipartFile file, String folderName) {

        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            String key = folderName + "/" + fileName;

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

            return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;

        } catch (IOException e) {
            throw new InvalidFileException("Failed to upload file to S3.");
        }
    }

    @Override
    public String uploadBytes(byte[] data, String contentType, String folderName) {

        String fileName = UUID.randomUUID() + ".jpg";
        String key = folderName + "/" + fileName;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(data));

        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;
    }

    @Override
    public void deleteFileByUrl(String fileUrl) {

        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        try {
            // Example URL:
            // https://bucket.s3.ap-south-1.amazonaws.com/products/full/abc.jpg
            String baseUrl = "https://" + bucketName + ".s3." + region + ".amazonaws.com/";

            if (!fileUrl.startsWith(baseUrl)) {
                throw new InvalidFileException("Invalid S3 file URL: " + fileUrl);
            }

            String key = fileUrl.substring(baseUrl.length());

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);

        } catch (Exception e) {
            throw new InvalidFileException("Failed to delete file from S3.");
        }
    }

}
