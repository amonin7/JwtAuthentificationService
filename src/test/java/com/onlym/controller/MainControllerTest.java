package com.onlym.controller;

import com.onlym.AuthServiceApplication;
import com.onlym.repo.UserRepository;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = AuthServiceApplication.class
)
@AutoConfigureMockMvc
class MainControllerTest {

    private final String user_pass = "test";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository repository;

    @Test
    @Order(1)
    public void createUser() throws Exception {
        String body = "{    \"username\": \"" + user_pass + "\",\n" +
                "    \"password\": \"" + user_pass + "\"}";
        mvc.perform(
                        post("/register")
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message",
                        is("User with username: '"+ user_pass + "' has been created")));

        assertTrue(repository.existsByUsername(user_pass));
    }

    @Test
    @Order(2)
    void authUser() throws Exception {
        String body = "{    \"username\": \"" + user_pass + "\",\n" +
                "    \"password\": \"" + user_pass + "\"}";
        mvc.perform(
                post("/register")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON));

        mvc.perform(
                post("/login")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username", is(user_pass)));
    }

    @Test
    @Order(3)
    void sayHello() throws Exception {
        // unauthorized
        mvc.perform(get("/hello"))
                .andExpect(status().is(401));
    }
}