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
            log.warn("Попытка создать бронирование с началом в прошлом: {}", start);
            throw new ValidationException("Начало бронирования не может быть в прошлом");
        }

        if (!end.isAfter(now)) {
            log.warn("Попытка создать бронирование с окончанием не в будущем: {}", end);
            throw new ValidationException("Окончание бронирования должно быть в будущем");
        }

        if (!end.isAfter(start)) {
            log.warn("Дата окончания {} не после даты начала {}", end, start);
            throw new ValidationException("Дата окончания должна быть после даты начала");
        }

        if (item.getOwner().getId() == userId) {
            log.warn("Владелец с id {} пытается забронировать свою вещь с id {}", userId, item.getId());
            throw new ValidationException("Владелец не может бронировать свою вещь");
        }

        if (!item.getAvailable()) {
            log.warn("Вещь с id {} недоступна для бронирования", item.getId());
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
        log.info("Создан booking: {}", saved);
        return BookingDtoMapper.toDto(saved);
    }

    @Override
    public BookingResponseDto approveBooking(long ownerId, long bookingId, boolean approved) {
        Booking booking = bookingRepository.findByIdWithBookerAndItem(bookingId);
        if (booking == null) {
            log.warn("Бронирование с id {} не найдено при подтверждении", bookingId);
            throw new NotFoundException("Booking не найден");
        }

        if (booking.getItem().getOwner().getId() != ownerId) {
            log.warn("Пользователь с id {} не является владельцем вещи для бронирования с id {}",
                    ownerId, bookingId);
            throw new ValidationException("Подтверждать может только владелец вещи");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            log.warn("Попытка изменить статус уже обработанного бронирования с id {}, текущий статус: {}",
                    bookingId, booking.getStatus());
            throw new ValidationException("Booking уже обработан");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        if (!approved) {
            Item item = booking.getItem();
            item.setAvailable(true);
            itemRepository.save(item);
            log.info("Вещь с id {} снова доступна для бронирования после отклонения бронирования с id {}",
                    item.getId(), bookingId);
        }

        Booking updated = bookingRepository.save(booking);
        log.info("Booking {} обновлён. Статус: {}", updated.getId(), updated.getStatus());
        return BookingDtoMapper.toDto(updated);
    }

    @Override
    public BookingResponseDto getBooking(long userId, long bookingId) {
        Booking booking = bookingRepository.findByIdWithBookerAndItem(bookingId);
        if (booking == null) {
            log.warn("Бронирование с id {} не найдено", bookingId);
            throw new NotFoundException("Booking не найден");
        }

        if (booking.getBooker().getId() != userId && booking.getItem().getOwner().getId() != userId) {
            log.warn("Пользователь с id {} не имеет доступа к бронированию с id {}", userId, bookingId);
            throw new NotFoundException("Нет доступа к этому бронированию");
        }

        log.info("Возвращено бронирование с id {} для пользователя с id {}", bookingId, userId);
        return BookingDtoMapper.toDto(booking);
    }

    @Override
    public List<BookingResponseDto> getUserBookings(long userId, String state) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User не найден"));

        List<Booking> bookings = bookingRepository.findByBookerIdOrderByStartDesc(userId);
        List<Booking> filteredBookings = filterByState(bookings, state);

        log.info("Найдено {} бронирований для пользователя с id {}, состояние: {}",
                filteredBookings.size(), userId, state);
        return filteredBookings.stream()
                .map(BookingDtoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(long ownerId, String state) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("User не найден"));

        List<Booking> bookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(ownerId);
        List<Booking> filteredBookings = filterByState(bookings, state);

        log.info("Найдено {} бронирований для владельца с id {}, состояние: {}",
                filteredBookings.size(), ownerId, state);
        return filteredBookings.stream()
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