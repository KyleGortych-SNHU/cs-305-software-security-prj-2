package com.snhu.sslserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.MessageDigest;

@SpringBootApplication
public class SslServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SslServerApplication.class, args);
    }
}

// Controller to handle hash computation
@RestController
class ServerController {

    // Get checksum for a static string (for initial use case)
    @GetMapping("/hash")
    public String myHash() {
        String data = "Kyle Gortych";
        String hash = "";
        String algorithmUsed = "SHA-256"; // SHA-256 for hashing

        try {
            MessageDigest digest = MessageDigest.getInstance(algorithmUsed);
            byte[] hashBytes = digest.digest(data.getBytes());
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }

            hash = sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "<p>Data: " + data + "</p>"
                + "<p>Cipher Used: " + algorithmUsed + "</p>"
                + "<p>CheckSum Value: " + hash + "</p>";
    }

    // POST endpoint for file uploads and checksum validation
    @PostMapping("/hash-file")
    public String hashFile(@RequestParam("file") MultipartFile file) {
        String hash = "";
        String algorithmUsed = "SHA-256";

        try {
            // Initialize the MessageDigest with the selected algorithm
            MessageDigest digest = MessageDigest.getInstance(algorithmUsed);
            
            // Getting the InputStream of the uploaded file
            InputStream inputStream = file.getInputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;

            // Read file 
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
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
            return "Error calculating checksum: " + e.getMessage();
        }

        // Return the result in HTML format
        return "<p>File Name: " + file.getOriginalFilename() + "</p>"
                + "<p>Checksum Algorithm Used: " + algorithmUsed + "</p>"
                + "<p>Checksum Value: " + hash + "</p>";
    }
}
