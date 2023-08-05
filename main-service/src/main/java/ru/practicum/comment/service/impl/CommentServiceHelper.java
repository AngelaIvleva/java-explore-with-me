package ru.practicum.comment.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.comment.enums.CommentStatus;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.exception.ConflictException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceHelper {

    private final CommentRepository commentRepository;

    public List<Comment> getCommentsByStatus(Long userId,
                                             CommentStatus status,
                                             Pageable pageable) {
        List<Comment> comments;
        if (status == null) {
            comments = commentRepository.findAllByAuthorId(userId, pageable);
        } else {
            comments = commentRepository.findAllByAuthorIdAndStatus(userId, status, pageable);
        }

        if (comments.isEmpty()) {
            throw new ConflictException(String.format("Нет комментариев по заданным параметрам: userId - %d, status - %s ",
                    userId, status));
        }
        log.info("Запрошены комментарии пользователя id {}, статус {}", userId, status);
        return comments;
    }
}
