package ru.practicum.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewCommentDto {
    @NotNull(message = "Текст комментаря не может быть пустым")
    @Size(min = 10, max = 500, message = "Min количество символов - 10, max - 500")
    private String text;
    @NotNull(message = "Поле isAnonymous не может быть пустым")
    private Boolean isAnonymous;
    private Long replyId;
}
