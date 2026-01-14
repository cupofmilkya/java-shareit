package shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shareit.request.dto.ItemRequestDto;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> postItemRequest(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @Valid @RequestBody ItemRequestDto dto
    ) {
        log.info("POST /requests userId={}, body={}", userId, dto);
        return itemRequestClient.postItemRequest(userId, dto);
    }

    @GetMapping
    public ResponseEntity<Object> getItemRequests(
            @RequestHeader("X-Sharer-User-Id") long userId
    ) {
        log.info("GET /requests userId={}", userId);
        return itemRequestClient.getItemRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllItemRequests(
            @RequestHeader("X-Sharer-User-Id") long userId
    ) {
        log.info("GET /requests/all userId={}", userId);
        return itemRequestClient.getAllItemRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getItemRequest(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PathVariable long requestId
    ) {
        log.info("GET /requests/{} userId={}", requestId, userId);
        return itemRequestClient.getItemRequest(userId, requestId);
    }
}