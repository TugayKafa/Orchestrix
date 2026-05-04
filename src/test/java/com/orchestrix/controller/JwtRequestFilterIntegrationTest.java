package com.orchestrix.controller;

import com.jayway.jsonpath.JsonPath;
import com.orchestrix.config.TestRedisConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestRedisConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class JwtRequestFilterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testRequestWithValidJwtTokenIsAccepted() throws Exception {
        String response = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "firstName": "Ivan",
                        "lastName": "Ivanov",
                        "email": "ivan@gmail.com",
                        "password": "Password123"
                    }
                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String accessToken = JsonPath.read(response, "$.accessToken");

        mockMvc.perform(get("/api/test")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testRequestWithInvalidJwtTokenIsDeclined() throws Exception {
        mockMvc.perform(get("/api/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRequestWithNullJwtTokenIsDeclined() throws Exception {
        mockMvc.perform(get("/api/test"))
                .andExpect(status().isUnauthorized());
    }

}
