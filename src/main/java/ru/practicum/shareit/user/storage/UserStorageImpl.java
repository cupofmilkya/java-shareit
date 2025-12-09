package ru.practicum.shareit.user.storage;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.handler.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.Map;

@Repository
@RequiredArgsConstructor
public class UserStorageImpl implements UserStorage {

    private static final Logger log = LoggerFactory.getLogger(UserStorageImpl.class);
    private final Map<Long, User> users;
    private final Map<String, Long> emailToUserId;

    @Override
    public User postUser(User user) {
        validateEmail(user.getEmail());

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
                validateEmail(patch.getEmail());

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

    private void validateEmail(String email) {
        if (emailToUserId.containsKey(email)) {
            log.warn("Попытка создания User с занятым email:{}", email);
            throw new IllegalArgumentException("Email уже используется");
        }
    }
}