package ru.practicum.comment.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentFullDto;
import ru.practicum.comment.dto.UpdateCommentAdminDto;
import ru.practicum.comment.enums.CommentStatus;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentLikeRepository;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.comment.service.AdminCommentService;
import ru.practicum.exception.ConflictException;
import ru.practicum.util.ObjectCheckExistence;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.comment.mapper.CommentMapper.COMMENT_MAPPER;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminCommentServiceImpl implements AdminCommentService {

    private final ObjectCheckExistence checkExistence;

    private final CommentRepository commentRepository;

    private final CommentLikeRepository likeRepository;
    private final CommentServiceHelper serviceHelper;

    @Override
    public CommentFullDto getByIdByAdmin(Long commentId) {
        log.info("Запрошен комментарий id {} /by Admin", commentId);
        return COMMENT_MAPPER.toCommentFullDto(checkExistence.checkComment(commentId));
    }

    @Override
    public List<CommentFullDto> getByUserIdByAdmin(Long userId, CommentStatus status, Pageable pageable) {
        checkExistence.checkUser(userId);
        List<Comment> comments = serviceHelper.getCommentsByStatus(userId, status, pageable);
        log.info("Запрошены комментарии пользователя id {} со статусом {} /by Admin", userId, status);
        return comments.stream()
                .map(COMMENT_MAPPER::toCommentFullDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentFullDto> getByEventIdByAdmin(Long eventId, CommentStatus status, Pageable pageable) {
        checkExistence.checkEvent(eventId);
        List<Comment> comments;
        if (status == null) {
            comments = commentRepository.findAllByEventId(eventId, pageable);
        } else {
            comments = commentRepository.findAllByEventIdAndStatus(eventId, status, pageable);
        }

        if (comments.isEmpty()) {
            throw new ConflictException(String.format("Нет комментариев по заданным параметрам: eventId - %d, status - %s ",
                    eventId, status));
        }

        log.info("Запрошены комментарии к событию id {} со статусом {} /by Admin", eventId, status);
        return comments.stream()
                .map(COMMENT_MAPPER::toCommentFullDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentFullDto updateCommentByAdmin(Long commentId,
                                               UpdateCommentAdminDto updateCommentByAdmin) {
        Comment comment = checkExistence.checkComment(commentId);
        comment.setStatus(updateCommentByAdmin.getStatus());
        return COMMENT_MAPPER.toCommentFullDto(commentRepository.save(comment));
    }

    @Transactional
    @Override
    public void deleteByIdByAdmin(Long commentId) {
        checkExistence.checkComment(commentId);
        likeRepository.deleteByCommentId(commentId);
        commentRepository.deleteByReplyId(commentId);
        log.info("Удаление комментария id {} /by Admin", commentId);
        commentRepository.deleteById(commentId);
    }
}
