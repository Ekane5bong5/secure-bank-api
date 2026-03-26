package com.kenzo.securebankapi.controller;

import com.kenzo.securebankapi.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/files")
public class FileUploadController {

    // 📍 Where files will be saved
    private static final String UPLOAD_DIR = "/tmp/uploads/";

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {

        try {
            String storedFileName = fileStorageService.storeFile(file);
            return ResponseEntity.ok("Uploaded: " + storedFileName);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}