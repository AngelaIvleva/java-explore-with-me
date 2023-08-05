package ru.practicum.comment.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.enums.CommentStatus;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentLikeRepository;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.comment.service.PrivateCommentService;
import ru.practicum.event.enums.State;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.user.model.User;
import ru.practicum.util.ObjectCheckExistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.comment.mapper.CommentMapper.COMMENT_MAPPER;

@Service
@Slf4j
@RequiredArgsConstructor
public class PrivateCommentServiceImpl implements PrivateCommentService {
    private final ObjectCheckExistence checkExistence;

    private final CommentRepository commentRepository;

    private final EventRepository eventRepository;

    private final CommentLikeRepository likeRepository;

    private final CommentServiceHelper serviceHelper;

    @Override
    public CommentDto createCommentByUser(Long eventId,
                                          Long userId,
                                          NewCommentDto commentDto) {
        validateReplyId(commentDto);

        User user = checkExistence.checkUser(userId);
        Event event = Optional.ofNullable(eventRepository.findByIdAndState(eventId, State.PUBLISHED)
                .orElseThrow(() -> new ConflictException("Комментировать можно только опубликованные события"))).get();

        Comment comment = Comment.builder()
                .text(commentDto.getText())
                .event(event)
                .author(user)
                .replyId(commentDto.getReplyId())
                .created(LocalDateTime.now())
                .status(CommentStatus.PENDING)
                .isAnonymous(commentDto.getIsAnonymous())
                .likes(0L)
                .dislikes(0L)
                .build();
        log.info("Добавление комментария пользователем {} к событию {}", userId, eventId);
        return COMMENT_MAPPER.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public List<CommentDto> getAllByUser(Long userId,
                                         CommentStatus status,
                                         Pageable pageable) {
        checkExistence.checkUser(userId);
        List<Comment> comments = serviceHelper.getCommentsByStatus(userId, status, pageable);
        log.info("Запрошены комментарии пользователя id {} со статусом {} /by User", userId, status);
        return comments.stream()
                .map(COMMENT_MAPPER::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto updateCommentByUser(Long commentId,
                                          Long eventId,
                                          Long userId,
                                          NewCommentDto updateComment) {
        User user = checkExistence.checkUser(userId);
        checkExistence.checkEvent(eventId);
        Comment comment = checkAuthor(commentId, user, userId);
        comment.setText(updateComment.getText());
        log.info("Обновление комментария {} пользователя {} к событию {}", commentId, userId, eventId);
        return COMMENT_MAPPER.toCommentDto(commentRepository.save(comment));
    }

    @Transactional
    @Override
    public void deleteCommentByUser(Long commentId,
                                    Long userId) {
        checkExistence.checkComment(commentId);
        User user = checkExistence.checkUser(userId);
        checkAuthor(commentId, user, userId);
        likeRepository.deleteByCommentId(commentId);
        commentRepository.deleteByReplyId(commentId);
        log.info("Удаление комментария {} пользователем {}", commentId, userId);
        commentRepository.deleteById(commentId);
    }


    private void validateReplyId(NewCommentDto commentDto) {
        Long replyId = commentDto.getReplyId();
        if (replyId != null) {
            checkExistence.checkComment(replyId);
        }
    }

    private Comment checkAuthor(Long commentId, User user, Long userId) {
        return commentRepository.findByIdAndAuthor(commentId, user).orElseThrow(
                () -> new ConflictException(String.format("Пользователь с id %d не является автором комментария id %d",
                        userId, commentId))
        );
    }
}
