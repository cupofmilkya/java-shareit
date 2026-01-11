package shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import shareit.user.dto.UserDto;
import shareit.user.validation.Create;
import shareit.user.validation.Update;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {

    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> postUser(
            @Validated(Create.class) @RequestBody UserDto userDto
    ) {
        log.info("POST /users body={}", userDto);
        return userClient.postUser(userDto);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUser(
            @PathVariable long userId
    ) {
        log.info("GET /users/{}", userId);
        return userClient.getUser(userId);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> updateUser(
            @PathVariable long userId,
            @Validated(Update.class) @RequestBody UserDto userDto
    ) {
        log.info("PATCH /users/{} body={}", userId, userDto);
        return userClient.updateUser(userId, userDto);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUser(
            @PathVariable long userId
    ) {
        log.info("DELETE /users/{}", userId);
        return userClient.deleteUser(userId);
    }
}