package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.validation.Create;

@Data
@Builder
public class ItemDto {
    private long id;

    @NotNull(groups = Create.class, message = "Поле name обязательно")
    @NotBlank(groups = Create.class)
    private String name;

    @NotNull(groups = Create.class, message = "Поле description обязательно")
    @NotBlank(groups = Create.class)
    private String description;

    @NotNull(groups = Create.class, message = "Поле available обязательно")
    private Boolean available;

    private User owner;

    private ItemRequest request;
}