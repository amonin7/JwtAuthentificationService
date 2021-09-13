package com.onlym.controller;

import com.onlym.config.jwt.JwtUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.FixMethodOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class MainControllerTest {

    private MainController mainController;

    private final static String user_pass = "test";
    private String jwt;

    private String getString(InputStream stream) {
        return new BufferedReader(new InputStreamReader(stream))
                .lines().collect(Collectors.joining("\n"));
    }

    @Autowired
    public MainControllerTest(MainController mainController) {
        this.mainController = mainController;
    }

    @BeforeEach
    void createTestUser() throws IOException {
        CloseableHttpClient client= new DefaultHttpClient();
        HttpPost request = new HttpPost("http://localhost:8080/register");
        String body = "{    \"username\": \"" + user_pass + "\",\n" +
                "    \"password\": \"" + user_pass + "\"}";

        StringEntity stringEntity = new StringEntity(body);
        stringEntity.setContentType("application/json");

        request.setHeader("Content-Type", "application/json");
        request.setHeader("Accept", "application/json");

        request.setEntity(stringEntity);
        client.execute(request);
    }

    @Test
    public void createUser() throws IOException {
        CloseableHttpClient client= new DefaultHttpClient();
        HttpPost request = new HttpPost("http://localhost:8080/register");
        String body = "{    \"username\": \"" + user_pass + "\",\n" +
                "    \"password\": \"" + user_pass + "\"}";

        StringEntity stringEntity = new StringEntity(body);
        stringEntity.setContentType("application/json");

        request.setHeader("Content-Type", "application/json");
        request.setHeader("Accept", "application/json");

        request.setEntity(stringEntity);

        CloseableHttpResponse resp = client.execute(request);
        String result = getString(resp.getEntity().getContent());
        assertEquals(
                resp.getStatusLine().getStatusCode(),
                HttpStatus.SC_BAD_REQUEST
        );
        assertTrue(result.contains("already exists"));
    }

    @Test
    public void authUser() throws IOException {
        CloseableHttpClient client= new DefaultHttpClient();
        HttpPost request = new HttpPost("http://localhost:8080/login");
        String body = "{    \"username\": \"" + user_pass + "\",\n" +
                "    \"password\": \"" + user_pass + "\"}";

        StringEntity stringEntity = new StringEntity(body);
        stringEntity.setContentType("application/json");

        request.setHeader("Content-Type", "application/json");
        request.setHeader("Accept", "application/json");

        request.setEntity(stringEntity);
        CloseableHttpResponse resp = client.execute(request);

        String result = getString(resp.getEntity().getContent());
        assertEquals(
                resp.getStatusLine().getStatusCode(),
                HttpStatus.SC_OK
        );
        assertTrue(result.contains("jwt"));
        assertTrue(result.contains("\"username\":\"" + user_pass + "\""));
    }

    @Test
    public void sayHello() throws IOException {
        CloseableHttpClient client= new DefaultHttpClient();
        HttpGet request = new HttpGet("http://localhost:8080/hello");
        request.setHeader("Authorization", "Bearer " + "THIS_IS_WRONG_JWT");
        CloseableHttpResponse resp = client.execute(request);
        assertEquals(
                resp.getStatusLine().getStatusCode(),
                HttpStatus.SC_UNAUTHORIZED
        );
    }

    @Test
    public void allIntegrationTest() throws IOException {
        // add user
        CloseableHttpClient client = new DefaultHttpClient();
        HttpPost request = new HttpPost("http://localhost:8080/register");
        String user_pass = "u7733";
        String body = "{    \"username\": \"" + user_pass + "\",\n" +
                "    \"password\": \"" + user_pass + "\"}";

        StringEntity stringEntity = new StringEntity(body);
        stringEntity.setContentType("application/json");

        request.setHeader("Content-Type", "application/json");
        request.setHeader("Accept", "application/json");
        request.setEntity(stringEntity);
        client.execute(request);

        // login user
        client = new DefaultHttpClient();
        request = new HttpPost("http://localhost:8080/login");
        body = "{    \"username\": \"" + user_pass + "\",\n" +
                "    \"password\": \"" + user_pass + "\"}";

        stringEntity = new StringEntity(body);
        stringEntity.setContentType("application/json");

        request.setHeader("Content-Type", "application/json");
        request.setHeader("Accept", "application/json");
        request.setEntity(stringEntity);
        CloseableHttpResponse resp = client.execute(request);

        String result = getString(resp.getEntity().getContent());
        jwt = result
                .replace("{\"jwt\":\"", "")
                .replace("\",\"username\":\"" + user_pass + "\"}", "");

        // get resource
        client = new DefaultHttpClient();
        HttpGet requestGet = new HttpGet("http://localhost:8080/hello");
        requestGet.setHeader("Authorization", "Bearer " + jwt);
        resp = client.execute(requestGet);
        assertEquals(
                resp.getStatusLine().getStatusCode(),
                HttpStatus.SC_OK
        );

    }
}