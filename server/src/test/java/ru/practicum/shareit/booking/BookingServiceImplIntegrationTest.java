package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.handler.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingServiceImpl bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User testOwner;
    private User testBooker;
    private Item testItem;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        itemRepository.deleteAll();
        bookingRepository.deleteAll();

        testOwner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@example.com")
                .build());

        testBooker = userRepository.save(User.builder()
                .name("Booker")
                .email("booker@example.com")
                .build());

        testItem = itemRepository.save(Item.builder()
                .name("Drill")
                .description("Power drill")
                .available(true)
                .owner(testOwner)
                .build());
    }

    private BookingRequestDto createTestBookingRequestDto() {
        return BookingRequestDto.builder()
                .itemId(testItem.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
    }

    @Test
    @DisplayName("Создание бронирования")
    void testCreateBooking() {
        BookingRequestDto bookingRequest = createTestBookingRequestDto();
        BookingResponseDto savedBooking = bookingService.createBooking(testBooker.getId(), bookingRequest);

        assertThat(savedBooking).isNotNull();
        assertThat(savedBooking.getId()).isNotNull();
        assertThat(savedBooking.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(savedBooking.getBooker().getId()).isEqualTo(testBooker.getId());
        assertThat(savedBooking.getItem().getId()).isEqualTo(testItem.getId());
    }

    @Test
    @DisplayName("Создание бронирования с началом в прошлом")
    void testCreateBookingWithPastStart() {
        BookingRequestDto bookingRequest = BookingRequestDto.builder()
                .itemId(testItem.getId())
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        assertThrows(jakarta.validation.ValidationException.class,
                () -> bookingService.createBooking(testBooker.getId(), bookingRequest));
    }

    @Test
    @DisplayName("Создание бронирования владельцем своей вещи")
    void testCreateBookingByOwner() {
        BookingRequestDto bookingRequest = createTestBookingRequestDto();

        assertThrows(jakarta.validation.ValidationException.class,
                () -> bookingService.createBooking(testOwner.getId(), bookingRequest));
    }

    @Test
    @DisplayName("Создание бронирования недоступной вещи")
    void testCreateBookingForUnavailableItem() {
        Item unavailableItem = itemRepository.save(Item.builder()
                .name("Hammer")
                .description("Broken hammer")
                .available(false)
                .owner(testOwner)
                .build());

        BookingRequestDto bookingRequest = BookingRequestDto.builder()
                .itemId(unavailableItem.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        assertThrows(jakarta.validation.ValidationException.class,
                () -> bookingService.createBooking(testBooker.getId(), bookingRequest));
    }

    @Test
    @DisplayName("Подтверждение бронирования")
    void testApproveBooking() {
        BookingRequestDto bookingRequest = createTestBookingRequestDto();
        BookingResponseDto savedBooking = bookingService.createBooking(testBooker.getId(), bookingRequest);

        BookingResponseDto approvedBooking = bookingService.approveBooking(testOwner.getId(), savedBooking.getId(), true);

        assertThat(approvedBooking).isNotNull();
        assertThat(approvedBooking.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    @DisplayName("Отклонение бронирования")
    void testRejectBooking() {
        BookingRequestDto bookingRequest = createTestBookingRequestDto();
        BookingResponseDto savedBooking = bookingService.createBooking(testBooker.getId(), bookingRequest);

        BookingResponseDto rejectedBooking = bookingService.approveBooking(testOwner.getId(), savedBooking.getId(), false);

        assertThat(rejectedBooking).isNotNull();
        assertThat(rejectedBooking.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    @DisplayName("Подтверждение бронирования не владельцем")
    void testApproveBookingByNonOwner() {
        User anotherUser = userRepository.save(User.builder()
                .name("Another User")
                .email("another@example.com")
                .build());

        BookingRequestDto bookingRequest = createTestBookingRequestDto();
        BookingResponseDto savedBooking = bookingService.createBooking(testBooker.getId(), bookingRequest);

        assertThrows(jakarta.validation.ValidationException.class,
                () -> bookingService.approveBooking(anotherUser.getId(), savedBooking.getId(), true));
    }

    @Test
    @DisplayName("Получение бронирования владельцем")
    void testGetBookingByOwner() {
        BookingRequestDto bookingRequest = createTestBookingRequestDto();
        BookingResponseDto savedBooking = bookingService.createBooking(testBooker.getId(), bookingRequest);

        BookingResponseDto retrievedBooking = bookingService.getBooking(testOwner.getId(), savedBooking.getId());

        assertThat(retrievedBooking).isNotNull();
        assertThat(retrievedBooking.getId()).isEqualTo(savedBooking.getId());
    }

    @Test
    @DisplayName("Получение бронирования арендатором")
    void testGetBookingByBooker() {
        BookingRequestDto bookingRequest = createTestBookingRequestDto();
        BookingResponseDto savedBooking = bookingService.createBooking(testBooker.getId(), bookingRequest);

        BookingResponseDto retrievedBooking = bookingService.getBooking(testBooker.getId(), savedBooking.getId());

        assertThat(retrievedBooking).isNotNull();
        assertThat(retrievedBooking.getId()).isEqualTo(savedBooking.getId());
    }

    @Test
    @DisplayName("Получение бронирования посторонним пользователем")
    void testGetBookingByOtherUser() {
        User otherUser = userRepository.save(User.builder()
                .name("Other User")
                .email("other@example.com")
                .build());

        BookingRequestDto bookingRequest = createTestBookingRequestDto();
        BookingResponseDto savedBooking = bookingService.createBooking(testBooker.getId(), bookingRequest);

        assertThrows(NotFoundException.class,
                () -> bookingService.getBooking(otherUser.getId(), savedBooking.getId()));
    }

    @Test
    @DisplayName("Получение бронирований пользователя")
    void testGetUserBookings() {
        BookingRequestDto bookingRequest = createTestBookingRequestDto();
        bookingService.createBooking(testBooker.getId(), bookingRequest);

        List<BookingResponseDto> userBookings = bookingService.getUserBookings(testBooker.getId(), "ALL");

        assertThat(userBookings).hasSize(1);
        assertThat(userBookings.getFirst().getBooker().getId()).isEqualTo(testBooker.getId());
    }

    @Test
    @DisplayName("Получение будущих бронирований пользователя")
    void testGetUserFutureBookings() {
        BookingRequestDto bookingRequest = createTestBookingRequestDto();
        bookingService.createBooking(testBooker.getId(), bookingRequest);

        List<BookingResponseDto> futureBookings = bookingService.getUserBookings(testBooker.getId(), "FUTURE");

        assertThat(futureBookings).hasSize(1);
    }

    @Test
    @DisplayName("Получение бронирований владельца")
    void testGetOwnerBookings() {
        BookingRequestDto bookingRequest = createTestBookingRequestDto();
        bookingService.createBooking(testBooker.getId(), bookingRequest);

        List<BookingResponseDto> ownerBookings = bookingService.getOwnerBookings(testOwner.getId(), "ALL");

        assertThat(ownerBookings).hasSize(1);
        assertThat(ownerBookings.getFirst().getItem().getOwner().getId()).isEqualTo(testOwner.getId());
    }
}