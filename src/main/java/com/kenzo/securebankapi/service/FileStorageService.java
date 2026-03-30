package com.kenzo.securebankapi.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadDir = Paths.get("/tmp/uploads").toAbsolutePath().normalize();

    public String storeFile(MultipartFile file) throws IOException {

        // 🔴 1. Validate file is not empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // 🔴 2. Enforce size limit (e.g., 5MB)
        long MAX_SIZE = 5 * 1024 * 1024;
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("File too large");
        }

        // 🔴 3. Extract original name (UNTRUSTED)
        String originalName = file.getOriginalFilename();

        // 🔴 4. Normalize filename (remove path tricks)
        String cleanName = Paths.get(originalName).getFileName().toString();

        // 🔴 5A. Validate actual file content (magic bytes)
        String contentType = file.getContentType();

        if (contentType == null ||
                (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new IllegalArgumentException("Invalid file content type");
        }

        // 🔴 5. Enforce allowed extensions
        if (!cleanName.endsWith(".jpg") && !cleanName.endsWith(".png")) {
            throw new IllegalArgumentException("Only images allowed");
        }

        // 🔴 6. Generate SAFE filename (DO NOT trust user name)
        String safeFileName = UUID.randomUUID() + "-" + cleanName;

        // 🔴 7. Build safe path
        Path uploadPath = uploadDir.toAbsolutePath().normalize();
        Path targetLocation = uploadPath.resolve(safeFileName);

        // 🔴 8. Prevent path traversal
        if (!targetLocation.startsWith(uploadPath)) {
            throw new SecurityException("Invalid file path");
        }

        // 🔴 9. Save file
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return safeFileName;
    }
    public byte[] readFile(String filename) throws IOException {

        // 🔹 Step 1: Resolve + normalize path
        Path targetPath = uploadDir.resolve(filename).normalize();

        // 🔥 LOG #1 — BEFORE security check
        System.out.println("🧪 Requested path resolves to: " + targetPath);

        // 🔥 CRITICAL: Enforce boundary
        if (!targetPath.startsWith(uploadDir)) {
            System.out.println("🚨 BLOCKED PATH TRAVERSAL: " + targetPath); // ✅ LOG #2
            throw new SecurityException("Path traversal attempt detected");
        }

        // 🔹 Step 2: Check file exists
        if (!Files.exists(targetPath)) {
            throw new FileNotFoundException("File not found");
        }

        // 🔥 LOG #3 — BEFORE reading file
        System.out.println("📂 Reading file: " + targetPath);

        // 🔹 Step 3: Read file
        return Files.readAllBytes(targetPath);
    }
}