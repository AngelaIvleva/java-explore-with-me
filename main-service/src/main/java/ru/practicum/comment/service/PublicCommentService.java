package ru.practicum.comment.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.comment.dto.CommentDto;

import java.util.List;

public interface PublicCommentService {

    CommentDto getByCommentIdByPublic(Long commentId);

    List<CommentDto> getByEventIdByPublic(Long eventId,
                                          Pageable pageable);

    List<CommentDto> getByUserIdByPublic(Long userId,
                                         Pageable pageable);

    List<CommentDto> getReplies(Long commentId,
                                Pageable pageable);
}
