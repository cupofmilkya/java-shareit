package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.user.validation.Create;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Long id;

    @NotNull(groups = Create.class, message = "Поле name обязательно")
    @NotBlank(groups = Create.class, message = "Поле name не может быть пустым")
    private String name;

    @NotNull(groups = Create.class, message = "Поле description обязательно")
    @NotBlank(groups = Create.class, message = "Поле description не может быть пустым")
    private String description;

    @NotNull(groups = Create.class, message = "Поле available обязательно")
    private Boolean available;

    private User owner;
    private ItemRequestDto request;

    private BookingForItemDto lastBooking;
    private BookingForItemDto nextBooking;

    private List<CommentDto> comments;
}