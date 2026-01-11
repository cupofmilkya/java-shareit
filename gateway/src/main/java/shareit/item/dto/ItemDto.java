package shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import shareit.booking.dto.BookingForItemDto;
import shareit.item.validation.Create;
import shareit.request.dto.ItemRequestDto;
import shareit.user.dto.UserDto;

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

    private UserDto owner;
    private ItemRequestDto request;

    private BookingForItemDto lastBooking;
    private BookingForItemDto nextBooking;

    private List<CommentDto> comments;
}