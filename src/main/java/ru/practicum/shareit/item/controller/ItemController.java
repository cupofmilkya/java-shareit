package ru.practicum.shareit.item.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.validation.Create;
import ru.practicum.shareit.user.validation.Update;

import java.util.List;

@RestController
@RequestMapping("/items")
@AllArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping()
    public ResponseEntity<ItemDto> postItem(@RequestHeader("X-Sharer-User-Id") long id,
                                            @Validated(Create.class) @RequestBody ItemDto itemDto) {
        ItemDto dto = itemService.postItem(id, itemDto);

        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> updateItem(@RequestHeader("X-Sharer-User-Id") long id,
                                              @PathVariable long itemId,
                                              @Validated(Update.class) @RequestBody ItemDto itemDto) {
        ItemDto dto = itemService.updateItem(id, itemId, itemDto);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDto> getItem(
            @RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
            @PathVariable long itemId) {
        ItemDto dto = itemService.getItem(itemId, userId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getItems(@RequestHeader("X-Sharer-User-Id") long id) {
        List<ItemDto> dtos = itemService.getItems(id);

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> getItemsByText(@RequestParam(required = true) String text) {
        List<ItemDto> dtos = itemService.getItemsByText(text);

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentDto> postComment(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PathVariable long itemId,
            @RequestBody CommentRequestDto commentRequestDto) {
        CommentDto commentDto = itemService.postComment(userId, itemId, commentRequestDto);

        return ResponseEntity.ok(commentDto);
    }
}