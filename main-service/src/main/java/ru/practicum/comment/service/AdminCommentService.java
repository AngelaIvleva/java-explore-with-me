package ru.practicum.comment.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.comment.dto.CommentFullDto;
import ru.practicum.comment.dto.UpdateCommentAdminDto;
import ru.practicum.comment.enums.CommentStatus;

import java.util.List;

public interface AdminCommentService {

    CommentFullDto getByIdByAdmin(Long commentId);

    List<CommentFullDto> getByUserIdByAdmin(Long userId,
                                            CommentStatus status,
                                            Pageable pageable);

    List<CommentFullDto> getByEventIdByAdmin(Long eventId,
                                             CommentStatus status,
                                             Pageable pageable);

    CommentFullDto updateCommentByAdmin(Long commentId,
                                        UpdateCommentAdminDto updateCommentByAdmin);

    void deleteByIdByAdmin(Long commentId);
}
