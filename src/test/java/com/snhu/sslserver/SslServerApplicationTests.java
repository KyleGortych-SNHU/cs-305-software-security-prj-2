package com.snhu.sslserver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Random;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class SslServerApplicationTests {

    private static final long MAX_FILE_SIZE = 10_000_000; // 10 MB
    private TestRestTemplate restTemplate;

    @BeforeEach
    void setup() throws Exception {
        restTemplate = buildSecureRestTemplate();
    }

    private TestRestTemplate buildSecureRestTemplate() throws Exception {
        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        String keystorePath = "src/main/resources/keystore.jks";
        String keystorePassword = "SNHU12";

        try (FileInputStream instream = new FileInputStream(new File(keystorePath))) {
            trustStore.load(instream, keystorePassword.toCharArray());
        }

        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(trustStore, null)
                .build();

        var tlsStrategy = new DefaultClientTlsStrategy(sslContext);

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(
                        PoolingHttpClientConnectionManagerBuilder.create()
                                .setTlsSocketStrategy(tlsStrategy)
                                .build()
                )
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        TestRestTemplate template = new TestRestTemplate();
        template.getRestTemplate().setRequestFactory(requestFactory);
        return template;
    }

    /* -----------------------------------------
       Basic Context & TLS Security
       ----------------------------------------- */

    @Test
    void contextLoads() {}

    @Test
    void testSecureHttpsConnection() {
        String response = restTemplate.getForObject("https://localhost:8443", String.class);
        assertNotNull(response);
    }

    /* -----------------------------------------
       File Upload Security Tests
       ----------------------------------------- */

    @Test
    void uploadEmptyFile_isHandledSafely() {
        ByteArrayResource emptyFile = createFile(new byte[0], "empty.txt");

        ResponseEntity<Map> response =
                restTemplate.postForEntity("https://localhost:8443/hash-file",
                        buildMultipartRequest(emptyFile), Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Uploaded file is empty.", response.getBody().get("error"));
    }

    @Test
    void maliciousFilename_isSanitizedCorrectly() {
        ByteArrayResource file = createFile("test".getBytes(), "<script>alert(1)</script>.txt");

        ResponseEntity<Map> response =
                restTemplate.postForEntity("https://localhost:8443/hash-file",
                        buildMultipartRequest(file), Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sanitize(file.getFilename()), response.getBody().get("filename"));
    }

    @Test
    void randomBinaryInput_doesNotCrashServer() {
        byte[] randomBytes = new byte[4096];
        new Random().nextBytes(randomBytes);
        ByteArrayResource file = createFile(randomBytes, "random.bin");

        ResponseEntity<Map> response =
                restTemplate.postForEntity("https://localhost:8443/hash-file",
                        buildMultipartRequest(file), Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sanitize(file.getFilename()), response.getBody().get("filename"));
        assertNotNull(response.getBody().get("checksum"));
    }

    @Test
    void oversizedFile_isRejected() {
        byte[] largeFile = new byte[15_000_000];
        ByteArrayResource file = createFile(largeFile, "too-big.bin");

        ResponseEntity<Map> response =
                restTemplate.postForEntity("https://localhost:8443/hash-file",
                        buildMultipartRequest(file), Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("File exceeds maximum allowed size.", response.getBody().get("error"));
    }

    /* -----------------------------------------
       Filename Sanitization Tests
       ----------------------------------------- */

    @Test
    void unicodeFilename_isSanitizedCorrectly() {
        ByteArrayResource file = createFile("test".getBytes(), "üñîçødé.txt");

        ResponseEntity<Map> response =
                restTemplate.postForEntity("https://localhost:8443/hash-file",
                        buildMultipartRequest(file), Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sanitize(file.getFilename()), response.getBody().get("filename"));
    }

    @Test
    void filenameWithSpaces_isSanitizedCorrectly() {
        ByteArrayResource file = createFile("test".getBytes(), "file with spaces.txt");

        ResponseEntity<Map> response =
                restTemplate.postForEntity("https://localhost:8443/hash-file",
                        buildMultipartRequest(file), Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sanitize(file.getFilename()), response.getBody().get("filename"));
    }

    @Test
    void filenameWithSpecialSymbols_isSanitizedCorrectly() {
        ByteArrayResource file = createFile("test".getBytes(), "file_@_symbol!.txt");

        ResponseEntity<Map> response =
                restTemplate.postForEntity("https://localhost:8443/hash-file",
                        buildMultipartRequest(file), Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sanitize(file.getFilename()), response.getBody().get("filename"));
    }

    @Test
    void longFilename_isHandledCorrectly() {
        String longName = "a".repeat(255) + ".txt";
        ByteArrayResource file = createFile("test".getBytes(), longName);

        ResponseEntity<Map> response =
                restTemplate.postForEntity("https://localhost:8443/hash-file",
                        buildMultipartRequest(file), Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sanitize(file.getFilename()), response.getBody().get("filename"));
    }

    @Test
    void maxSizeFile_isAccepted() {
        byte[] maxSizeFile = new byte[(int) MAX_FILE_SIZE];
        new Random().nextBytes(maxSizeFile);
        ByteArrayResource file = createFile(maxSizeFile, "max-size.bin");

        ResponseEntity<Map> response =
                restTemplate.postForEntity("https://localhost:8443/hash-file",
                        buildMultipartRequest(file), Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sanitize(file.getFilename()), response.getBody().get("filename"));
        assertNotNull(response.getBody().get("checksum"));
    }

    /* -----------------------------------------
       Checksum Verification Test
       ----------------------------------------- */

    @Test
    void checksumIsCorrectForKnownFile() throws Exception {
        File file = new File("src/test/resources/testfile.txt");
        byte[] fileBytes;
        try (FileInputStream fis = new FileInputStream(file)) {
            fileBytes = fis.readAllBytes();
        }

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(fileBytes);
        StringBuilder expectedChecksum = new StringBuilder();
        for (byte b : hashBytes) {
            expectedChecksum.append(String.format("%02x", b));
        }

        ByteArrayResource resource = createFile(fileBytes, "testfile.txt");

        ResponseEntity<Map> response =
                restTemplate.postForEntity("https://localhost:8443/hash-file",
                        buildMultipartRequest(resource), Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedChecksum.toString(), response.getBody().get("checksum"));
        assertEquals(sanitize(resource.getFilename()), response.getBody().get("filename"));
    }

    /* -----------------------------------------
       Helper Methods
       ----------------------------------------- */

    private HttpEntity<?> buildMultipartRequest(ByteArrayResource file) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file);

        return new HttpEntity<>(body, headers);
    }

    private String sanitize(String filename) {
        if (filename == null) return null;
        return Paths.get(filename).getFileName()
                .toString()
                .replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    // ----- New helper to simplify ByteArrayResource creation -----
    private ByteArrayResource createFile(byte[] content, String filename) {
        return new ByteArrayResource(content) {
            @Override
            public String getFilename() { return filename; }
        };
    }
}
