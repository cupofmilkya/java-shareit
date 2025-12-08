package ru.practicum.shareit.user.mapper;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

public class UserDtoMapper {
    public static User toModel(UserDto dto) {
        return User.builder()
                .id(dto.getId())
                .name(dto.getName())
                .email(dto.getEmail())
                .build();
    }

    public static UserDto toDto(User model) {
        return UserDto.builder()
                .id(model.getId())
                .name(model.getName())
                .email(model.getEmail())
                .build();
    }
}
