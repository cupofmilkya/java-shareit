package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.handler.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.Map;

@Repository
public class UserStorageImpl implements UserStorage {

    private final Map<Long, User> users;
    private final Map<String, Long> emailToUserId;

    public UserStorageImpl() {
        users = new HashMap<>();
        emailToUserId = new HashMap<>();
    }

    @Override
    public User postUser(User user) {
        if (emailToUserId.containsKey(user.getEmail())) {
            throw new IllegalArgumentException("Email уже используется");
        }

        user.setId(generateId());

        users.put(user.getId(), user);
        emailToUserId.put(user.getEmail(), user.getId());


        return user;
    }

    @Override
    public User getUser(long userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        return user;
    }

    @Override
    public User updateUser(long userId, User patch) {
        User user = users.get(userId);

        if (user == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        if (patch.getEmail() != null) {
            if (!patch.getEmail().equals(user.getEmail())) {
                if (emailToUserId.containsKey(patch.getEmail())) {
                    throw new IllegalArgumentException("Email уже занят");
                }

                emailToUserId.remove(user.getEmail());
                emailToUserId.put(patch.getEmail(), userId);

                user.setEmail(patch.getEmail());
            }
        }

        if (patch.getName() != null) {
            user.setName(patch.getName());
        }

        return user;
    }

    @Override
    public void deleteUser(long userId) {
        User user = users.remove(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        emailToUserId.remove(user.getEmail());
    }

    private long generateId() {
        return users.size() + 1;
    }
}
