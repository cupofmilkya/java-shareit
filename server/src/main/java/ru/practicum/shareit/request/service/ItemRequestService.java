package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestResponseDto postItemRequest(ItemRequestDto dto, long userId);

    List<ItemRequestResponseDto> getItemRequests(long userId);

    List<ItemRequestResponseDto> getAllItemRequests();

    ItemRequestResponseDto getItemRequest(long id);
}
