package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.handler.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserServiceImpl;
import ru.practicum.shareit.user.storage.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserServiceImplIntegrationTest {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    private UserDto createTestUserDto() {
        return UserDto.builder()
                .name("Test User")
                .email("test@example.com")
                .build();
    }

    @Test
    @DisplayName("Создание пользователя")
    void testPostUser() {
        UserDto userDto = createTestUserDto();
        UserDto savedUser = userService.postUser(userDto);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("Test User");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Создание пользователя с уже существующим email")
    void testPostUserWithDuplicateEmail() {
        UserDto userDto = createTestUserDto();
        userService.postUser(userDto);

        UserDto duplicateUserDto = UserDto.builder()
                .name("Another User")
                .email("test@example.com")
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> userService.postUser(duplicateUserDto));
    }

    @Test
    @DisplayName("Получение пользователя по ID")
    void testGetUser() {
        UserDto userDto = createTestUserDto();
        UserDto savedUser = userService.postUser(userDto);
        UserDto retrievedUser = userService.getUser(savedUser.getId());

        assertThat(retrievedUser).isNotNull();
        assertThat(retrievedUser.getId()).isEqualTo(savedUser.getId());
        assertThat(retrievedUser.getName()).isEqualTo("Test User");
        assertThat(retrievedUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Получение несуществующего пользователя")
    void testGetNonExistentUser() {
        assertThrows(NotFoundException.class,
                () -> userService.getUser(999L));
    }

    @Test
    @DisplayName("Обновление имени пользователя")
    void testUpdateUserName() {
        UserDto userDto = createTestUserDto();
        UserDto savedUser = userService.postUser(userDto);

        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .build();

        UserDto updatedUser = userService.updateUser(savedUser.getId(), updateDto);

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getId()).isEqualTo(savedUser.getId());
        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Обновление email пользователя")
    void testUpdateUserEmail() {
        UserDto userDto = createTestUserDto();
        UserDto savedUser = userService.postUser(userDto);

        UserDto updateDto = UserDto.builder()
                .email("updated@example.com")
                .build();

        UserDto updatedUser = userService.updateUser(savedUser.getId(), updateDto);

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getId()).isEqualTo(savedUser.getId());
        assertThat(updatedUser.getName()).isEqualTo("Test User");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    @DisplayName("Обновление email на уже существующий")
    void testUpdateUserWithDuplicateEmail() {
        UserDto user1 = UserDto.builder()
                .name("User 1")
                .email("user1@example.com")
                .build();
        UserDto savedUser1 = userService.postUser(user1);

        UserDto user2 = UserDto.builder()
                .name("User 2")
                .email("user2@example.com")
                .build();
        UserDto savedUser2 = userService.postUser(user2);

        UserDto updateDto = UserDto.builder()
                .email("user1@example.com")
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(savedUser2.getId(), updateDto));
    }

    @Test
    @DisplayName("Обновление всех полей пользователя")
    void testUpdateAllUserFields() {
        UserDto userDto = createTestUserDto();
        UserDto savedUser = userService.postUser(userDto);

        UserDto updateDto = UserDto.builder()
                .name("Updated User")
                .email("updated@example.com")
                .build();

        UserDto updatedUser = userService.updateUser(savedUser.getId(), updateDto);

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getId()).isEqualTo(savedUser.getId());
        assertThat(updatedUser.getName()).isEqualTo("Updated User");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    @DisplayName("Удаление пользователя")
    void testDeleteUser() {
        UserDto userDto = createTestUserDto();
        UserDto savedUser = userService.postUser(userDto);

        userService.deleteUser(savedUser.getId());

        assertThrows(NotFoundException.class,
                () -> userService.getUser(savedUser.getId()));
    }

    @Test
    @DisplayName("Удаление несуществующего пользователя")
    void testDeleteNonExistentUser() {
        assertThrows(NotFoundException.class,
                () -> userService.deleteUser(999L));
    }
}