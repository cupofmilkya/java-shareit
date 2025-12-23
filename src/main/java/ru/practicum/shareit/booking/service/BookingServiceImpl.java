package ru.practicum.shareit.booking.service;

import jakarta.validation.ValidationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingDtoMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.handler.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingResponseDto createBooking(long userId, BookingRequestDto bookingRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User не найден"));

        Item item = itemRepository.findById(bookingRequestDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Item не найден"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = bookingRequestDto.getStart();
        LocalDateTime end = bookingRequestDto.getEnd();

        if (start.isBefore(now)) {
            throw new ValidationException("Начало бронирования не может быть в прошлом");
        }

        if (!end.isAfter(now)) {
            throw new ValidationException("Окончание бронирования должно быть в будущем");
        }

        if (!end.isAfter(start)) {
            throw new ValidationException("Дата окончания должна быть после даты начала");
        }

        if (item.getOwner().getId() == userId) {
            throw new ValidationException("Владелец не может бронировать свою вещь");
        }

        if (!item.getAvailable()) {
            throw new ValidationException("Item недоступен для бронирования");
        }

        Booking booking = Booking.builder()
                .booker(user)
                .item(item)
                .start(start)
                .end(end)
                .status(BookingStatus.WAITING)
                .build();

        Booking saved = bookingRepository.save(booking);
        log.info("Создан booking {}", saved);
        return BookingDtoMapper.toDto(saved);
    }

    @Override
    public BookingResponseDto approveBooking(long ownerId, long bookingId, boolean approved) {
        Booking booking = bookingRepository.findByIdWithBookerAndItem(bookingId);
        if (booking == null) throw new NotFoundException("Booking не найден");

        if (booking.getItem().getOwner().getId() != ownerId) {
            throw new ValidationException("Подтверждать может только владелец вещи");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Booking уже обработан");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        if (!approved) {
            Item item = booking.getItem();
            item.setAvailable(true);
            itemRepository.save(item);
        }

        Booking updated = bookingRepository.save(booking);
        log.info("Booking {} обновлён. Статус: {}", updated.getId(), updated.getStatus());
        return BookingDtoMapper.toDto(updated);
    }

    @Override
    public BookingResponseDto getBooking(long userId, long bookingId) {
        Booking booking = bookingRepository.findByIdWithBookerAndItem(bookingId);
        if (booking == null) throw new NotFoundException("Booking не найден");

        if (booking.getBooker().getId() != userId && booking.getItem().getOwner().getId() != userId) {
            throw new NotFoundException("Нет доступа к этому бронированию");
        }

        return BookingDtoMapper.toDto(booking);
    }

    @Override
    public List<BookingResponseDto> getUserBookings(long userId, String state) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User не найден"));

        List<Booking> bookings = bookingRepository.findByBookerIdOrderByStartDesc(userId);
        return filterByState(bookings, state).stream()
                .map(BookingDtoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(long ownerId, String state) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("User не найден"));

        List<Booking> bookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(ownerId);
        return filterByState(bookings, state).stream()
                .map(BookingDtoMapper::toDto)
                .collect(Collectors.toList());
    }

    private List<Booking> filterByState(List<Booking> bookings, String state) {
        LocalDateTime now = LocalDateTime.now();
        return bookings.stream().filter(b -> {
            return switch (state.toUpperCase()) {
                case "CURRENT" -> b.getStart().isBefore(now) && b.getEnd().isAfter(now);
                case "PAST" -> b.getEnd().isBefore(now);
                case "FUTURE" -> b.getStart().isAfter(now);
                case "WAITING" -> b.getStatus() == BookingStatus.WAITING;
                case "REJECTED" -> b.getStatus() == BookingStatus.REJECTED;
                default -> true;
            };
        }).collect(Collectors.toList());
    }
}