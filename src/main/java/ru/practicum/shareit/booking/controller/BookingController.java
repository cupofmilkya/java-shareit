package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@AllArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponseDto> createBooking(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestBody @Valid BookingRequestDto bookingRequestDto) {
        return ResponseEntity.ok(bookingService.createBooking(userId, bookingRequestDto));
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> approveBooking(
            @RequestHeader("X-Sharer-User-Id") long ownerId,
            @PathVariable long bookingId,
            @RequestParam boolean approved) {
        return ResponseEntity.ok(bookingService.approveBooking(ownerId, bookingId, approved));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> getBooking(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PathVariable long bookingId) {
        return ResponseEntity.ok(bookingService.getBooking(userId, bookingId));
    }

    @GetMapping
    public ResponseEntity<List<BookingResponseDto>> getUserBookings(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(defaultValue = "ALL") String state) {
        return ResponseEntity.ok(bookingService.getUserBookings(userId, state));
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingResponseDto>> getOwnerBookings(
            @RequestHeader("X-Sharer-User-Id") long ownerId,
            @RequestParam(defaultValue = "ALL") String state) {
        return ResponseEntity.ok(bookingService.getOwnerBookings(ownerId, state));
    }
}