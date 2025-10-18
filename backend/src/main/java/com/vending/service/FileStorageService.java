package com.vending.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    // Security: Allowed MIME types for images
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/gif",
        "image/webp"
    );

    // Security: Maximum file size (10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB in bytes

    public FileStorageService(@Value("${file.upload-dir:uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file, String category) {
        // Security: Validate file is not empty
        if (file.isEmpty()) {
            throw new RuntimeException("Cannot store empty file");
        }

        // Security: Validate file size (prevent DoS attacks)
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("File size exceeds maximum allowed size of " + (MAX_FILE_SIZE / 1024 / 1024) + "MB");
        }

        // Security: Validate MIME type (prevent malicious file uploads)
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new RuntimeException("Invalid file type. Only image files (JPEG, PNG, GIF, WebP) are allowed");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = "";

        if (originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // Security: Validate file extension matches MIME type
        if (!isValidExtensionForMimeType(fileExtension, contentType)) {
            throw new RuntimeException("File extension does not match file type");
        }

        // Generate unique filename
        String fileName = category + "-" + UUID.randomUUID().toString() + fileExtension;

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new RuntimeException("Invalid file path: " + fileName);
            }

            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    private boolean isValidExtensionForMimeType(String extension, String mimeType) {
        extension = extension.toLowerCase();
        mimeType = mimeType.toLowerCase();

        switch (mimeType) {
            case "image/jpeg":
            case "image/jpg":
                return extension.equals(".jpg") || extension.equals(".jpeg");
            case "image/png":
                return extension.equals(".png");
            case "image/gif":
                return extension.equals(".gif");
            case "image/webp":
                return extension.equals(".webp");
            default:
                return false;
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            // Validate filename to prevent path traversal
            if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
                throw new RuntimeException("Invalid file path: " + fileName);
            }

            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();

            // Ensure the resolved path is still within the upload directory
            if (!filePath.startsWith(this.fileStorageLocation)) {
                throw new RuntimeException("Invalid file path: " + fileName);
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File not found " + fileName, ex);
        }
    }

    public void deleteFile(String fileName) {
        try {
            // Validate filename to prevent path traversal
            if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
                throw new RuntimeException("Invalid file path: " + fileName);
            }

            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();

            // Ensure the resolved path is still within the upload directory
            if (!filePath.startsWith(this.fileStorageLocation)) {
                throw new RuntimeException("Invalid file path: " + fileName);
            }

            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Could not delete file " + fileName, ex);
        }
    }
}
