package com.onlym;

import com.google.gson.Gson;
import com.onlym.model.response.JwtResponse;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = AuthServiceApplication.class
)
@AutoConfigureMockMvc
class AuthServiceApplicationTests {

    private final String user_pass = "test";

    @Autowired
    private MockMvc mvc;

	@Test
	void contextLoads() {
	}

    @Test
    public void allIntegrationTest() throws Exception {
        String body = "{    \"username\": \"" + user_pass + "\",\n" +
                "    \"password\": \"" + user_pass + "\"}";
        mvc.perform(
                post("/register")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON));

        MvcResult result = mvc.perform(
                        post("/login")
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username", is(user_pass)))
                .andReturn();

        JwtResponse jwtResponse = new Gson().fromJson(result.getResponse().getContentAsString(), JwtResponse.class);

        mvc.perform(
                        get("/hello")
                                .header("Authorization", "Bearer " + jwtResponse.getJwt()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("Hello, " + user_pass + "!")));

    }

}
