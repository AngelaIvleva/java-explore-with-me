package ru.practicum.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserShortDto {
    @NotBlank(message = "Имя не может быть пустым")
    @NotNull(message = "Имя не может быть пустым")
    @Size(min = 2, max = 250)
    private String name;
    @Email(message = "Некорректный адрес электронной почты")
    @NotNull(message = "Адрес электронной почты не может быть пустым")
    @Size(min = 6, max = 254)
    private String email;
}
