package com.snhu.sslserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class SslServerApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void testSecureConnection() throws Exception {
        // Load the truststore - must match application.properties settings
        KeyStore trustStore = KeyStore.getInstance("PKCS12");  // Changed from JKS to PKCS12
        String keystorePath = "src/main/resources/keystore.jks";
        String keystorePassword = "SNHU12";
        
        try (FileInputStream instream = new FileInputStream(new File(keystorePath))) {
            trustStore.load(instream, keystorePassword.toCharArray());
        }

        // Build SSL context with the truststore
        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(trustStore, null)
                .build();

        // Create TLS strategy directly (non-deprecated way)
        var tlsStrategy = new DefaultClientTlsStrategy(sslContext);

        // Create HTTP client with SSL support
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(
                        PoolingHttpClientConnectionManagerBuilder.create()
                                .setTlsSocketStrategy(tlsStrategy)
                                .build()
                )
                .build();

        // Create request factory with the HTTP client
        HttpComponentsClientHttpRequestFactory requestFactory = 
                new HttpComponentsClientHttpRequestFactory(httpClient);

        // Create RestTemplate with the custom request factory
        TestRestTemplate restTemplate = new TestRestTemplate();
        restTemplate.getRestTemplate().setRequestFactory(requestFactory);

        // Test the connection
        String response = restTemplate.getForObject("https://localhost:8443", String.class);
        assertNotNull(response);
    }
}
