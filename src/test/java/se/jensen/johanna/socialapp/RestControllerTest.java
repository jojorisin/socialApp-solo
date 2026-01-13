package se.jensen.johanna.socialapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import se.jensen.johanna.socialapp.SocialAppApplication;
import se.jensen.johanna.socialapp.dto.UserRequest;
import se.jensen.johanna.socialapp.model.User;
import se.jensen.johanna.socialapp.repository.PostRepository;
import se.jensen.johanna.socialapp.repository.UserRepository;

import java.util.List;

@SpringBootTest(classes = SocialAppApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class RestControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;


    @Test
    void createUserTest() throws Exception {
        UserRequest userRequest = new UserRequest(
                "ipod@test.com", "johanna", "password", "password"
        );
        String jsondata = objectMapper.writeValueAsString(userRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsondata))
                .andExpect(MockMvcResultMatchers.status().isCreated());

        List<User> users = userRepository.findAll();
        System.out.println(users.size());
        Assertions.assertEquals(1, users.size());
    }
}
