package ru.practicum.shareit.item.mapper;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;

import java.util.Collections;
import java.util.List;

@Slf4j
public class ItemDtoMapper {
    public static Item toModel(ItemDto dto) {
        return Item.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .available(dto.getAvailable())
                .owner(dto.getOwner())
                .request(dto.getRequest() != null ? ItemRequestMapper.toEntity(dto.getRequest()) : null)
                .build();
    }

    public static ItemDto toDto(Item model) {
        return toDto(model, null, null, Collections.emptyList());
    }

    public static ItemDto toDto(Item model,
                                BookingForItemDto lastBooking,
                                BookingForItemDto nextBooking,
                                List<CommentDto> comments) {
        return ItemDto.builder()
                .id(model.getId())
                .name(model.getName())
                .description(model.getDescription())
                .available(model.getAvailable())
                .owner(model.getOwner())
                .request(model.getRequest() != null ?
                        ItemRequestMapper.toDto(model.getRequest()) : null)
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(comments != null ? comments : Collections.emptyList())
                .build();
    }
}