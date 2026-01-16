package com.app.Livetracker.service;
import com.app.Livetracker.exception.ImageUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.core.sync.RequestBody;
//import com.e.backend.exception.ImageUploadException;
import org.springframework.scheduling.annotation.Async;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;


@Service
public class S3Service {
    private final S3Client s3Client;

    @Value("${aws.bucket.name}")
    private String bucketName;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Async("s3Executor")
    public CompletableFuture<String> upload(
            MultipartFile file,
            String folder) {

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";

            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String key = folder + "/" + UUID.randomUUID() + extension;

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

            String imageUrl = "https://" + bucketName + ".s3.amazonaws.com/" + key;

            System.out.println("UPLOAD THREAD : " + Thread.currentThread().getName());

            return CompletableFuture.completedFuture(imageUrl);

        } catch (Exception e) {
            throw new ImageUploadException("S3 upload failed: " + e.getMessage());
        }
    }
    @Async("s3Executor")
    public void delete(String imageKey) {
        try {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(imageKey)
                            .build()
            );
            System.out.println("Deleted: " + imageKey + " on thread: " + Thread.currentThread().getName());
        } catch (Exception e) {
            // log and retry if needed
            System.err.println("Failed to delete " + imageKey);
        }
    }
}




//
//package com.app.Livetracker.service;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//import software.amazon.awssdk.core.sync.RequestBody;
//import software.amazon.awssdk.services.s3.S3Client;
//import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
//import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
//import software.amazon.awssdk.services.s3.model.PutObjectRequest;
//
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class S3Service {
//
//    private final S3Client s3Client;
//
//    @Value("${aws.bucket.name}")
//    private String bucketName;
//
//    /**
//     * Upload file to S3
//     * @param file MultipartFile to upload
//     * @param folder Folder name in S3 (e.g., "products")
//     * @return Public URL of uploaded file
//     */
//    public String upload(MultipartFile file, String folder) {
//        try {
//            String originalFilename = file.getOriginalFilename();
//            String extension = "";
//            if (originalFilename != null && originalFilename.contains(".")) {
//                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
//            }
//            String key = folder + "/" + UUID.randomUUID() + extension;
//
//            PutObjectRequest request = PutObjectRequest.builder()
//                    .bucket(bucketName)
//                    .key(key)
//                    .contentType(file.getContentType())
//                    .acl(ObjectCannedACL.PUBLIC_READ)
//                    .build();
//
//            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
//
//            // include region in URL if necessary
//            return "https://" + bucketName + ".s3.amazonaws.com/" + key;
//
//        } catch (Exception e) {
//            throw new RuntimeException("S3 upload failed: " + e.getMessage());
//        }
//    }
//
//    /**
//     * Delete file from S3
//     * @param fileUrl Public URL of the file
//     */
//    public void delete(String fileUrl) {
//        if (fileUrl == null || fileUrl.isEmpty()) return;
//
//        // Extract key from URL
//        String key = fileUrl.substring(fileUrl.indexOf(".com/") + 5);
//
//        DeleteObjectRequest request = DeleteObjectRequest.builder()
//                .bucket(bucketName)
//                .key(key)
//                .build();
//
//        s3Client.deleteObject(request);
//    }
//}
