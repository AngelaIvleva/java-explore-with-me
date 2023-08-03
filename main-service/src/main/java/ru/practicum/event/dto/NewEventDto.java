package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.location.model.Location;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {
    @NotNull(message = "Аннотация не может быть пустой")
    @Size(min = 20, max = 2000, message = "Min количество символов аннотации - 20, max - 2000")
    private String annotation;
    @NotNull(message = "Категория не может быть пустой")
    private Long category;
    @NotNull(message = "Описание не может быть пустым")
    @Size(min = 20, max = 7000, message = "Min количество символов описания - 20, max - 7000")
    private String description;
    @NotNull(message = "Дата события не может быть пустой")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    @NotNull(message = "Локация не может быть пустой")
    private Location location;
    private boolean paid = false;
    private int participantLimit;
    private Boolean requestModeration = true;
    @NotNull(message = "Заголовок не может быть пустым")
    @Size(min = 3, max = 120, message = "Min количество символов заголовка - 3, max - 120")
    private String title;
}
