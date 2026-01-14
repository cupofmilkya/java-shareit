package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingRequestDto testBookingRequestDto;
    private BookingResponseDto testBookingResponseDto;

    @BeforeEach
    void setUp() {
        testBookingRequestDto = BookingRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        UserDto bookerDto = UserDto.builder()
                .id(1L)
                .name("Booker")
                .email("booker@example.com")
                .build();

        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("Power drill")
                .available(true)
                .build();

        testBookingResponseDto = BookingResponseDto.builder()
                .id(1L)
                .start(testBookingRequestDto.getStart())
                .end(testBookingRequestDto.getEnd())
                .status(BookingStatus.WAITING)
                .booker(bookerDto)
                .item(itemDto)
                .build();
    }

    @Test
    @DisplayName("Создание бронирования")
    void testCreateBooking() throws Exception {
        when(bookingService.createBooking(eq(1L), any(BookingRequestDto.class)))
                .thenReturn(testBookingResponseDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBookingRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.booker.id").value(1L))
                .andExpect(jsonPath("$.item.id").value(1L));

        verify(bookingService, times(1)).createBooking(eq(1L), any(BookingRequestDto.class));
    }

    @Test
    @DisplayName("Создание бронирования без заголовка пользователя")
    void testCreateBookingWithoutUserId() throws Exception {
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBookingRequestDto)))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).createBooking(anyLong(), any(BookingRequestDto.class));
    }

    @Test
    @DisplayName("Подтверждение бронирования")
    void testApproveBooking() throws Exception {
        BookingResponseDto approvedBooking = BookingResponseDto.builder()
                .id(1L)
                .status(BookingStatus.APPROVED)
                .build();

        when(bookingService.approveBooking(eq(1L), eq(1L), eq(true)))
                .thenReturn(approvedBooking);

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(bookingService, times(1)).approveBooking(1L, 1L, true);
    }

    @Test
    @DisplayName("Отклонение бронирования")
    void testRejectBooking() throws Exception {
        BookingResponseDto rejectedBooking = BookingResponseDto.builder()
                .id(1L)
                .status(BookingStatus.REJECTED)
                .build();

        when(bookingService.approveBooking(eq(1L), eq(1L), eq(false)))
                .thenReturn(rejectedBooking);

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(bookingService, times(1)).approveBooking(1L, 1L, false);
    }

    @Test
    @DisplayName("Получение бронирования")
    void testGetBooking() throws Exception {
        when(bookingService.getBooking(eq(1L), eq(1L)))
                .thenReturn(testBookingResponseDto);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("WAITING"));

        verify(bookingService, times(1)).getBooking(1L, 1L);
    }

    @Test
    @DisplayName("Получение бронирования без заголовка пользователя")
    void testGetBookingWithoutUserId() throws Exception {
        mockMvc.perform(get("/bookings/1"))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).getBooking(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Получение бронирований пользователя")
    void testGetUserBookings() throws Exception {
        when(bookingService.getUserBookings(eq(1L), eq("ALL")))
                .thenReturn(List.of(testBookingResponseDto));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(bookingService, times(1)).getUserBookings(1L, "ALL");
    }

    @Test
    @DisplayName("Получение бронирований пользователя без заголовка")
    void testGetUserBookingsWithoutUserId() throws Exception {
        mockMvc.perform(get("/bookings"))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).getUserBookings(anyLong(), anyString());
    }

    @Test
    @DisplayName("Получение бронирований пользователя с состоянием")
    void testGetUserBookingsWithState() throws Exception {
        when(bookingService.getUserBookings(eq(1L), eq("FUTURE")))
                .thenReturn(List.of(testBookingResponseDto));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "FUTURE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(bookingService, times(1)).getUserBookings(1L, "FUTURE");
    }

    @Test
    @DisplayName("Получение бронирований владельца")
    void testGetOwnerBookings() throws Exception {
        when(bookingService.getOwnerBookings(eq(1L), eq("ALL")))
                .thenReturn(List.of(testBookingResponseDto));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(bookingService, times(1)).getOwnerBookings(1L, "ALL");
    }

    @Test
    @DisplayName("Получение бронирований владельца без заголовка")
    void testGetOwnerBookingsWithoutUserId() throws Exception {
        mockMvc.perform(get("/bookings/owner"))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).getOwnerBookings(anyLong(), anyString());
    }
}