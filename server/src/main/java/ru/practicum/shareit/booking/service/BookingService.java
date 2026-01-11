package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {
    BookingResponseDto createBooking(long userId, BookingRequestDto bookingRequestDto);

    BookingResponseDto approveBooking(long ownerId, long bookingId, boolean approved);

    BookingResponseDto getBooking(long userId, long bookingId);

    List<BookingResponseDto> getUserBookings(long userId, String state);

    List<BookingResponseDto> getOwnerBookings(long ownerId, String state);
}