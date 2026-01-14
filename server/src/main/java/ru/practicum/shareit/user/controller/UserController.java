package ru.practicum.shareit.user.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.dto.UserDto;

@RestController
@RequestMapping(path = "/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> postUser(@RequestBody UserDto userDto) {
        UserDto dto = userService.postUser(userDto);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUser(@PathVariable long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(@PathVariable long userId,
                                              @RequestBody UserDto userDto) {
        UserDto dto = userService.updateUser(userId, userDto);

        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable long userId) {
        userService.deleteUser(userId);

        return ResponseEntity.ok().build();
    }
}
