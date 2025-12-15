package com.snhu.sslserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.HttpStatusCodeException;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ServerApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * Method to get an insecure RestTemplate that accepts self-signed certificates.
     */
    private TestRestTemplate getInsecureRestTemplate() throws NoSuchAlgorithmException, KeyManagementException {
        // Trust all certificates (For testing only - not recommended for production)
        TrustManager[] trustAllCertificates = new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
        };

        // Set up SSL context with our trust manager
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCertificates, new java.security.SecureRandom());
        SSLSocketFactory factory = sslContext.getSocketFactory();

        // Create RestTemplate and set its request factory to use the custom SSL context
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new org.springframework.http.client.HttpComponentsClientHttpRequestFactory(
                org.apache.http.impl.client.HttpClients.custom()
                        .setSSLContext(sslContext)
                        .build()));

        return new TestRestTemplate(restTemplate);
    }

    /**
     * Test to ensure /hash endpoint returns expected data over HTTPS.
     * Uses the insecure RestTemplate to bypass SSL validation for self-signed certificates.
     */
    @Test
    void hashEndpointReturnsData() {
        try {
            // Use the insecure RestTemplate that bypasses SSL certificate validation
            TestRestTemplate insecureRestTemplate = getInsecureRestTemplate();

            // Perform GET request on the /hash endpoint (Assumes server is running on HTTPS)
            ResponseEntity<String> response = insecureRestTemplate.getForEntity("https://localhost:8443/hash", String.class);

            // Check if the response contains expected data (e.g., name and hash algorithm)
            assertTrue(response.getBody().contains("Kyle Gortych"));
            assertTrue(response.getBody().contains("SHA-256"));
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            fail("SSL initialization failed: " + e.getMessage());
        } catch (HttpStatusCodeException e) {
            fail("HTTP error occurred: " + e.getMessage());
        }
    }

    /**
     * Test to check if the Spring Boot application context loads successfully.
     */
    @Test
    void contextLoads() {
        // No actual logic here, just checks if the Spring context loads without issues
    }
}
