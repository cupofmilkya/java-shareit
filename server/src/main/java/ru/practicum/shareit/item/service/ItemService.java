package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto postItem(long id, ItemDto itemDto);

    ItemDto updateItem(long id, long itemId, ItemDto itemDto);

    ItemDto getItem(long id);

    ItemDto getItem(long itemId, Long userId);

    List<ItemDto> getItems(long id);

    List<ItemDto> getItemsByText(String text);

    CommentDto postComment(long userId, long itemId, CommentRequestDto commentRequestDto);
}
