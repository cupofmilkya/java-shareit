package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import shareit.ShareItGateway;
import shareit.booking.BookingClient;
import shareit.booking.BookingController;
import shareit.booking.dto.BookItemRequestDto;
import shareit.booking.dto.BookingState;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
@ContextConfiguration(classes = {ShareItGateway.class, BookingController.class})
class BookingGatewayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingClient bookingClient;

    @Test
    @DisplayName("Создание бронирования с валидными данными")
    void testCreateBookingWithValidData() throws Exception {
        BookItemRequestDto validDto = new BookItemRequestDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        when(bookingClient.bookItem(anyLong(), any(BookItemRequestDto.class)))
                .thenReturn(org.springframework.http.ResponseEntity.ok().build());

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isOk());

        verify(bookingClient).bookItem(eq(1L), any(BookItemRequestDto.class));
    }

    @Test
    @DisplayName("Создание бронирования с датой начала в прошлом")
    void testCreateBookingWithPastStartDate() throws Exception {
        BookItemRequestDto invalidDto = new BookItemRequestDto(
                1L,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(bookingClient, never()).bookItem(anyLong(), any());
    }

    @Test
    @DisplayName("Создание бронирования с датой окончания в прошлом")
    void testCreateBookingWithPastEndDate() throws Exception {
        BookItemRequestDto invalidDto = new BookItemRequestDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().minusDays(1)
        );

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(bookingClient, never()).bookItem(anyLong(), any());
    }

    @Test
    @DisplayName("Создание бронирования без заголовка userId")
    void testCreateBookingWithoutHeader() throws Exception {
        BookItemRequestDto validDto = new BookItemRequestDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isInternalServerError());

        verify(bookingClient, never()).bookItem(anyLong(), any());
    }

    @Test
    @DisplayName("Получение бронирований с отрицательным from")
    void testGetBookingsWithInvalidFrom() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getBookings(anyLong(), any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Получение бронирований с size = 0")
    void testGetBookingsWithInvalidSize() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getBookings(anyLong(), any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Получение бронирований с неизвестным статусом")
    void testGetBookingsWithInvalidState() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .param("state", "INVALID_STATE")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getBookings(anyLong(), any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Получение бронирований с валидными параметрами")
    void testGetBookingsWithValidParams() throws Exception {
        when(bookingClient.getBookings(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(org.springframework.http.ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(bookingClient).getBookings(eq(1L), eq(BookingState.ALL), eq(0), eq(10));
    }

    @Test
    @DisplayName("Получение бронирования по id")
    void testGetBookingById() throws Exception {
        when(bookingClient.getBooking(anyLong(), anyLong()))
                .thenReturn(org.springframework.http.ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings/100")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk());

        verify(bookingClient).getBooking(1L, 100L);
    }

    @Test
    @DisplayName("Подтверждение бронирования")
    void testApproveBooking() throws Exception {
        when(bookingClient.approveBooking(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(org.springframework.http.ResponseEntity.ok().build());

        mockMvc.perform(patch("/bookings/100")
                        .header("X-Sharer-User-Id", "1")
                        .param("approved", "true"))
                .andExpect(status().isOk());

        verify(bookingClient).approveBooking(1L, 100L, true);
    }
}