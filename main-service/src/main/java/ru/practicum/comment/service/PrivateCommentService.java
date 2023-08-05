package ru.practicum.comment.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.enums.CommentStatus;

import java.util.List;

public interface PrivateCommentService {

    CommentDto createCommentByUser(Long eventId,
                                   Long userId,
                                   NewCommentDto commentDto);

    List<CommentDto> getAllByUser(Long userId,
                                  CommentStatus status,
                                  Pageable pageable);

    CommentDto updateCommentByUser(Long commentId,
                                   Long eventId,
                                   Long userId,
                                   NewCommentDto updateComment);

    void deleteCommentByUser(Long commentId,
                             Long userId);

}
