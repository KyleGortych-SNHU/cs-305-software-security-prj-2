package com.snhu.sslserver;

import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class SslServerApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    // Test for the /hash endpoint that computes checksum for static data
    @Test
    void hashEndpointReturnsChecksumOverHttps() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "https://localhost:8443/hash", // Ensure HTTPS is used
                String.class
        );

        // Check if the response contains expected values
        assertTrue(response.getBody().contains("Kyle Gortych"));
        assertTrue(response.getBody().contains("SHA-256"));
    }

    // Test for the /hash-file endpoint to ensure checksum is returned for uploaded file
    @Test
    void hashFileEndpointReturnsChecksum() throws Exception {
        // Read the test file
        Path path = Path.of("src/test/resources/testfile.txt");  // File used for testing
        File file = path.toFile();

        // Read the file content into a byte array
        byte[] fileContent = Files.readAllBytes(path);

        // Create a MockMultipartFile from the file content
        MockMultipartFile mockFile = new MockMultipartFile("file", file.getName(), "text/plain", fileContent);

        // Prepare the form data (file upload)
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", mockFile);  // Add the MockMultipartFile to the form data

        // Set up the HTTP headers to handle multipart data
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Create an HttpEntity with the headers and the file body
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        // Create the POST request to upload the file and get the checksum
        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://localhost:8443/hash-file", entity, String.class);

        // Check that the response contains checksum information
        assertTrue(response.getBody().contains("Checksum Value:"));
        assertTrue(response.getBody().contains(file.getName()));  // Check if file name is included
    }

    // Ensure that the application context loads correctly
    @Test
    void contextLoads() {
    }

    // Configure TestRestTemplate to accept self-signed certificates
    @Autowired
    public void setRestTemplate(TestRestTemplate restTemplate) throws Exception {
        // Load the self-signed certificate (adjust the path to your cert)
        X509Certificate certificate = loadCertificate("src/test/resources/self-signed-cert.pem");

        // Create a KeyStore and add the certificate
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);  // No password for the keystore
        keyStore.setCertificateEntry("selfSigned", certificate);

        // Set up SSL context with the custom KeyStore (only the self-signed certificate)
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new javax.net.ssl.TrustManager[] {new javax.net.ssl.X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            public void checkClientTrusted(X509Certificate[] certs, String authType) {}
            public void checkServerTrusted(X509Certificate[] certs, String authType) {}
        }}, new java.security.SecureRandom());

        // Create an HttpClient with NoopHostnameVerifier to allow self-signed certs
        HttpClient httpClient = HttpClients.custom()
                .setSslcontext(sslContext)
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE) // Disable hostname verification for testing
                .build();

        // Use HttpComponentsClientHttpRequestFactory to wrap the HttpClient
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        // Set the factory to the TestRestTemplate's underlying RestTemplate
        restTemplate.getRestTemplate().setRequestFactory(factory);
    }

    private X509Certificate loadCertificate(String certPath) throws Exception {
        try (var is = Files.newInputStream(Path.of(certPath))) {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) certificateFactory.generateCertificate(is);
        }
    }
}
