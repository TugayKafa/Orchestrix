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
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestRedisConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AuthControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void testRegisterWithValidInputData()
            throws Exception {

        registerUser("Ivan", "Ivanov", "ivan@gmail.com", "Password123")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.email").value("ivan@gmail.com"));
    }

    @Test
    void testRegisterWithExistingEmail()
            throws Exception {

        registerUser("Ivan", "Ivanov", "ivan@gmail.com", "Password123")
                .andExpect(status().isCreated());

        registerUser("Ivan", "Ivanov", "ivan@gmail.com", "Password123")
                .andExpect(status().isConflict());
    }

    @Test
    void testLoginWithValidInputData()
            throws Exception {

        registerUser("Ivan", "Ivanov", "ivan@gmail.com", "Password123")
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "email": "ivan@gmail.com",
                        "password": "Password123"
                    }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.email").value("ivan@gmail.com"));

    }

    @Test
    void testLoginWithInvalidEmail()
            throws Exception {

        registerUser("Ivan", "Ivanov", "ivan@gmail.com", "Password123")
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "email": "invalid@gmail.com",
                        "password": "Password123"
                    }
                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void testLoginWithInvalidPassword()
            throws Exception {

        registerUser("Ivan", "Ivanov", "ivan@gmail.com", "Password123")
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "email": "ivan@gmail.com",
                        "password": "wrongPassword"
                    }
                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRefreshWithValidRefreshToken()
            throws Exception {

        String response = registerUser("Ivan", "Ivanov", "ivan@gmail.com", "Password123")
                            .andExpect(status().isCreated())
                            .andReturn().getResponse().getContentAsString();
        String refreshToken = JsonPath.read(response, "$.refreshToken");

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                    {
                        "token": "%s"
                    }
                """, refreshToken)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    void testRefreshWithInvalidRefreshToken()
            throws Exception {

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "token": "invalid-token"
                    }
                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void testLogoutRevokesTokensSuccessfully()
            throws Exception {

        String response = registerUser("Ivan", "Ivanov", "ivan@gmail.com", "Password123")
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String refreshToken = JsonPath.read(response, "$.refreshToken");
        String accessToken = JsonPath.read(response, "$.accessToken");

        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                    {
                        "token": "%s"
                    }
                """, refreshToken)))
                .andExpect(status().isNoContent());
    }

    private ResultActions registerUser(String firstName, String lastName, String email, String password)
            throws Exception {

        return mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                    {
                        "firstName": "%s",
                        "lastName": "%s",
                        "email": "%s",
                        "password": "%s"
                    }
                """, firstName, lastName, email, password)));
    }
}
