package shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import shareit.item.validation.Create;
import shareit.item.validation.Update;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private long id;

    @NotBlank(groups = Create.class, message = "Имя не может быть пустым")
    private String name;

    @Email(groups = {Create.class, Update.class}, message = "Некорректный email")
    @NotBlank(groups = Create.class, message = "Email не может быть пустым")
    private String email;
}