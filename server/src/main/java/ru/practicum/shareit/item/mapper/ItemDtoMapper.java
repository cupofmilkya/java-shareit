package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.Collections;
import java.util.List;

public class ItemDtoMapper {

    public static Item toModel(ItemDto dto) {
        return toModel(dto, null);
    }

    public static Item toModel(ItemDto dto, ItemRequest request) {
        Item item = new Item();
        item.setId(dto.getId());
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setAvailable(dto.getAvailable());
        item.setRequest(request);
        return item;
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