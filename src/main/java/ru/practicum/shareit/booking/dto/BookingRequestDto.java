package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestDto {

    @NotNull(message = "itemId не может быть пустым")
    private Long itemId;

    @NotNull(message = "start не может быть пустым")
    @FutureOrPresent(message = "start не может быть в прошлом")
    private LocalDateTime start;

    @NotNull(message = "end не может быть пустым")
    @Future(message = "end должен быть в будущем")
    private LocalDateTime end;
}