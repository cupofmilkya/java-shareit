package shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ItemRequestDto {
    private long id;

    @NotBlank(message = "Описание запроса не может быть пустым")
    private String description;

    private long requestorId;
    private LocalDateTime created;
}