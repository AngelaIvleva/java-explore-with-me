package ru.practicum.compilation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.event.dto.EventShortDto;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompilationDto {

    @NotNull(message = "Поле id не может быть null")
    private Long id;
    private List<EventShortDto> events;
    @NotNull(message = "Закрепление подборки на главной странице сайта не может быть null")
    private Boolean pinned;
    @NotNull(message = "Заголовок подборки не может быть null")
    private String title;
}
