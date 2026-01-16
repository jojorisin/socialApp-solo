package se.jensen.johanna.socialapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import se.jensen.johanna.socialapp.dto.LoginRequestDTO;
import se.jensen.johanna.socialapp.dto.RefreshTokenRequest;
import se.jensen.johanna.socialapp.dto.RegisterUserRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void getToken() throws Exception {
        RegisterUserRequest registerRequest = new RegisterUserRequest(
                "test@example.com",
                "testuser",
                "password123",
                "password123"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // 2. Förbered login-anropet
        LoginRequestDTO loginRequest = new LoginRequestDTO("testuser", "password123");

        // 3. Utför POST /auth/login (som anropar din getToken-metod)
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print()) // Skriver ut hela svaret i konsolen för felsökning
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void refreshToken() throws Exception {
        // 1. Registrera en användare för att få en giltig refreshToken
        RegisterUserRequest regRequest = new RegisterUserRequest(
                "refresh@test.com", "refreshuser", "password123", "password123"
        );

        String registerResponseJson = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Hämta ut den refreshToken som skapades vid registreringen
        String refreshToken = JsonPath.read(registerResponseJson, "$.refreshToken");

        // 2. Förbered refresh-anropet
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(refreshToken);

        // 3. Utför POST /auth/refresh
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                // Verifiera att vi får tillbaka samma refreshToken (enligt din kod)
                .andExpect(jsonPath("$.refreshToken").value(refreshToken));
    }

    @Test
    void register() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest("test@test.com", "test", "password", "password");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

}
