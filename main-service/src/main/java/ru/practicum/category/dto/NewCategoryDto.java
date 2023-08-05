package ru.practicum.category.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewCategoryDto {
    @NotNull(message = "Наименование категории не может быть пустым")
    @NotBlank(message = "Наименование категории не может быть пустым")
    @Size(min = 1, max = 50, message = "Min количество символов нименования категории - 1, max - 50")
    private String name;
}
