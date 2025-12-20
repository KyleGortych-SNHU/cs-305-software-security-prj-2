package com.snhu.sslserver;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

import java.io.InputStream;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ServerController {

    private static final long MAX_FILE_SIZE = 10_000_000; // 10 MB
    private static final String HASH_ALGORITHM = "SHA-256";

    @PostMapping("/hash-file")
    public ResponseEntity<Map<String, String>> hashFile(
            @RequestParam("file") MultipartFile file) {

        Map<String, String> response = new HashMap<>();

        // Input Validation
        if (file == null || file.isEmpty()) {
            response.put("error", "Uploaded file is empty.");
            return ResponseEntity.badRequest().body(response);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            response.put("error", "File exceeds maximum allowed size.");
            return ResponseEntity.badRequest().body(response);
        }

        // Sanitize filename to prevent injection & traversal issues
        String safeFilename = Paths.get(file.getOriginalFilename())
                .getFileName()
                .toString()
                .replaceAll("[^a-zA-Z0-9._-]", "_"); // replaces unsafe characters

        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);

            try (InputStream inputStream = file.getInputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }

            byte[] hashBytes = digest.digest();
            StringBuilder checksum = new StringBuilder();
            for (byte b : hashBytes) {
                checksum.append(String.format("%02x", b));
            }

            response.put("filename", safeFilename);
            response.put("algorithm", HASH_ALGORITHM);
            response.put("checksum", checksum.toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Log internally; do not expose stack traces
            System.err.println("Checksum calculation failed: " + e.getMessage());

            response.put("error", "Unable to process uploaded file.");
            return ResponseEntity.status(500).body(response);
        }
    }
}
