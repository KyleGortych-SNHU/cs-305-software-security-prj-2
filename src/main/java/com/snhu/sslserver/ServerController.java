package com.snhu.sslserver;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.ResponseEntity;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ServerController {

    @PostMapping("/hash-file")
    public ResponseEntity<Map<String, String>> hashFile(@RequestParam("file") MultipartFile file) {
        String hash = "";
        String algorithmUsed = "SHA-256";
        Map<String, String> response = new HashMap<>();

        try {
            // Initialize the MessageDigest with the selected algorithm
            MessageDigest digest = MessageDigest.getInstance(algorithmUsed);
            
            // Get the InputStream once and use try-with-resources to ensure it's closed
            try (InputStream inputStream = file.getInputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;

                // Read file 
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }

            // Get final hash value
            byte[] hashBytes = digest.digest();
            StringBuilder sb = new StringBuilder();

            // Convert the byte array into a hexadecimal string
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }

            hash = sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Error calculating checksum: " + e.getMessage());
            return ResponseEntity.status(500).body(response); // Return error status
        }

        // Return the result as JSON
        response.put("filename", file.getOriginalFilename());
        response.put("algorithm", algorithmUsed);
        response.put("checksum", hash);

        return ResponseEntity.ok(response); // Return successful response
    }
}
