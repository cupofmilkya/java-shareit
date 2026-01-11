package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {

    public static ItemRequestResponseDto toDto(ItemRequest request) {
        if (request == null) return null;

        List<ItemDto> items = request.getItems() != null ?
                request.getItems().stream()
                        .map(item -> ItemDto.builder()
                                .id(item.getId())
                                .name(item.getName())
                                .description(item.getDescription())
                                .available(item.getAvailable())
                                .request(ItemRequestMapper.toDto(request))
                                .build())
                        .collect(Collectors.toList()) :
                Collections.emptyList();

        return ItemRequestResponseDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .requestorId(request.getRequestor() != null ? request.getRequestor().getId() : 0)
                .created(request.getCreated())
                .items(items)
                .build();
    }

    public static ItemRequest toEntity(ItemRequestResponseDto dto) {
        if (dto == null) return null;

        ItemRequest request = new ItemRequest();
        request.setId(dto.getId());
        request.setDescription(dto.getDescription());
        request.setCreated(dto.getCreated());
        return request;
    }

    public static ItemRequest toEntity(ItemRequestDto dto) {
        if (dto == null) return null;
        ItemRequest request = new ItemRequest();
        request.setDescription(dto.getDescription());
        return request;
    }
}