package ru.practicum.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.comment.enums.CommentStatus;
import ru.practicum.event.model.Event;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentFullDto {
    private Long id;
    private String text;
    private Event event;
    private User author;
    private Long replyId;
    private LocalDateTime created;
    private CommentStatus status;
    private Boolean isAnonymous;
    private Long likes;
    private Long dislikes;
}
