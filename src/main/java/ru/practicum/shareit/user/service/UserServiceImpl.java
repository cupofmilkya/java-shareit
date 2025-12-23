package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserDtoMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;

    @Override
    public UserDto postUser(UserDto dto) {
        User user = userStorage.postUser(UserDtoMapper.toModel(dto));
        log.info("Создан User:{}", user);

        return UserDtoMapper.toDto(user);
    }

    @Override
    public UserDto getUser(long userId) {
        User user = userStorage.getUser(userId);

        return UserDtoMapper.toDto(user);
    }

    @Override
    public UserDto updateUser(long userId, UserDto dto) {
        User user = userStorage.updateUser(userId, UserDtoMapper.toModel(dto));
        log.info("Обновлен User:{}", user);

        return UserDtoMapper.toDto(user);
    }

    @Override
    public void deleteUser(long userId) {
        log.info("Удален User с id {}", userId);
        userStorage.deleteUser(userId);
    }
}