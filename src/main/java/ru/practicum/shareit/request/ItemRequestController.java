package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/requests")
@Slf4j
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ResponseEntity<ItemRequestResponseDto> postItemRequest(
            @Valid @RequestBody ItemRequestDto dto,
            @RequestHeader("X-Sharer-User-Id") long id
    ) {
        log.info("POST /requests - получен запрос от пользователя {}: {}", id, dto);
        ItemRequestResponseDto response = itemRequestService.postItemRequest(dto, id);
        log.info("Успешно создан запрос: {}", response);
        return ResponseEntity.ok(response);
    }

    @GetMapping()
    public ResponseEntity<List<ItemRequestResponseDto>> getItemRequests(
            @RequestHeader("X-Sharer-User-Id") long id
    ) {
        return ResponseEntity.ok(itemRequestService.getItemRequests(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ItemRequestResponseDto>> getItemRequests() {
        return ResponseEntity.ok(itemRequestService.getAllItemRequests());
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ItemRequestResponseDto> getItemRequest(
            @PathVariable long requestId
    ) {
        return ResponseEntity.ok(itemRequestService.getItemRequest(requestId));
    }
}
