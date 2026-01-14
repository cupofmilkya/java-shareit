package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

public interface UserService {

    UserDto postUser(UserDto dto);

    UserDto getUser(long userId);

    UserDto updateUser(long userId, UserDto dto);

    void deleteUser(long userId);
}
