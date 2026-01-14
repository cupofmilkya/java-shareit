package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

public interface UserStorage {

    User postUser(User dto);

    User getUser(long userId);

    User updateUser(long userId, User dto);

    void deleteUser(long userId);
}
