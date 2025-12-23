package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.user.dto.UserDto;

public class BookingDtoMapper {

    public static BookingResponseDto toDto(Booking booking) {
        if (booking == null) return null;

        ItemDto itemDto = ItemDto.builder()
                .id(booking.getItem().getId())
                .name(booking.getItem().getName())
                .description(booking.getItem().getDescription())
                .available(booking.getItem().getAvailable())
                .owner(booking.getItem().getOwner())
                .request(booking.getItem().getRequest() != null ? ItemRequestMapper.toDto(booking.getItem().getRequest()) : null)
                .build();

        UserDto bookerDto = UserDto.builder()
                .id(booking.getBooker().getId())
                .name(booking.getBooker().getName())
                .email(booking.getBooker().getEmail())
                .build();

        return BookingResponseDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .item(itemDto)
                .booker(bookerDto)
                .build();
    }
}