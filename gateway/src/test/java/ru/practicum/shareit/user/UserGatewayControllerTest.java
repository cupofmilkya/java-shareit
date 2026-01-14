package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import shareit.ShareItGateway;
import shareit.user.UserClient;
import shareit.user.UserController;
import shareit.user.dto.UserDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@ContextConfiguration(classes = {ShareItGateway.class, UserController.class})
class UserGatewayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserClient userClient;

    @Test
    @DisplayName("Создание пользователя через gateway с пустым именем")
    void testPostUserWithEmptyName() throws Exception {
        UserDto invalidUserDto = UserDto.builder()
                .name("")  // пустое имя
                .email("valid@email.com")  // валидный email
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDto)))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).postUser(any(UserDto.class));
    }

    @Test
    @DisplayName("Создание пользователя через gateway с невалидным email")
    void testPostUserWithInvalidEmail() throws Exception {
        UserDto invalidUserDto = UserDto.builder()
                .name("Valid Name")
                .email("invalid-email")  // невалидный email
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDto)))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).postUser(any(UserDto.class));
    }

    @Test
    @DisplayName("Создание пользователя через gateway с пустым email")
    void testPostUserWithEmptyEmail() throws Exception {
        UserDto invalidUserDto = UserDto.builder()
                .name("Valid Name")
                .email("")  // пустой email
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDto)))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).postUser(any(UserDto.class));
    }

    @Test
    @DisplayName("Обновление пользователя через gateway с невалидным email")
    void testUpdateUserWithInvalidEmail() throws Exception {
        UserDto invalidUserDto = UserDto.builder()
                .email("invalid-email")  // невалидный email
                .build();

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDto)))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).updateUser(eq(1L), any(UserDto.class));
    }

    @Test
    @DisplayName("Создание пользователя через gateway с валидными данными")
    void testPostUserWithValidData() throws Exception {
        UserDto validUserDto = UserDto.builder()
                .name("Valid Name")
                .email("valid@email.com")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserDto)))
                .andExpect(status().isOk());

        verify(userClient).postUser(any(UserDto.class));
    }

    @Test
    @DisplayName("Обновление пользователя через gateway с валидными данными")
    void testUpdateUserWithValidData() throws Exception {
        UserDto validUserDto = UserDto.builder()
                .name("Updated Name")
                .email("updated@email.com")
                .build();

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserDto)))
                .andExpect(status().isOk());

        verify(userClient).updateUser(eq(1L), any(UserDto.class));
    }

    @Test
    @DisplayName("Обновление пользователя через gateway только с именем")
    void testUpdateUserWithNameOnly() throws Exception {
        UserDto validUserDto = UserDto.builder()
                .name("Only Name Updated")
                .build();

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserDto)))
                .andExpect(status().isOk());

        verify(userClient).updateUser(eq(1L), any(UserDto.class));
    }

    @Test
    @DisplayName("Обновление пользователя через gateway только с email")
    void testUpdateUserWithEmailOnly() throws Exception {
        UserDto validUserDto = UserDto.builder()
                .email("onlyemail@email.com")
                .build();

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserDto)))
                .andExpect(status().isOk());

        verify(userClient).updateUser(eq(1L), any(UserDto.class));
    }
}