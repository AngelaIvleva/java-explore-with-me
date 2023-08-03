package ru.practicum.comment.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.comment.service.PublicCommentService;
import ru.practicum.exception.ConflictException;
import ru.practicum.util.ObjectCheckExistence;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.comment.enums.CommentStatus.PUBLISHED;
import static ru.practicum.comment.mapper.CommentMapper.COMMENT_MAPPER;

@Service
@Slf4j
@RequiredArgsConstructor
public class PublicCommentServiceImpl implements PublicCommentService {

    private final ObjectCheckExistence checkExistence;

    private final CommentRepository commentRepository;

    @Override
    public CommentDto getByCommentIdByPublic(Long commentId) {
        Comment comment = commentRepository.findByIdAndStatus(commentId, PUBLISHED)
                .orElseThrow(() -> new ConflictException("Доступны к просмотру только опубликованные комментарии"));
        CommentDto commentDto = COMMENT_MAPPER.toCommentDto(comment);

        if (comment.getIsAnonymous()) {
            commentDto.setAuthorName("Anonymous");
        }

        log.info("Запрошен комментарий id {}", commentId);
        return commentDto;
    }

    @Override
    public List<CommentDto> getByEventIdByPublic(Long eventId, Pageable pageable) {
        List<Comment> comments = commentRepository.findAllByEventIdAndStatus(eventId, PUBLISHED, pageable);

        checkExistence.checkCommentsExists(comments);

        log.info("Запрошены опубликованные комментарии к событию {}", eventId);
        return setAnonAuthorName(comments);
    }

    @Override
    public List<CommentDto> getByUserIdByPublic(Long userId, Pageable pageable) {
        List<Comment> comments = commentRepository.findAllByAuthorIdAndStatus(userId, PUBLISHED, pageable);

        checkExistence.checkCommentsExists(comments);

        log.info("Запрошены опубликованные комментарии пользователя {}", userId);
        return setAnonAuthorName(comments);
    }

    @Override
    public List<CommentDto> getReplies(Long commentId,
                                       Pageable pageable) {
        Comment comment = checkExistence.checkComment(commentId);

        List<Comment> comments = commentRepository.findByReplyIdAndStatus(commentId, PUBLISHED, pageable);

        checkExistence.checkCommentsExists(comments);

        comments.add(0, comment);
        return comments.stream()
                .map(COMMENT_MAPPER::toCommentDto)
                .collect(Collectors.toList());
    }

    private List<CommentDto> setAnonAuthorName(List<Comment> comments) {
        return comments.stream()
                .map(comment -> {
                    CommentDto commentDto = COMMENT_MAPPER.toCommentDto(comment);
                    if (comment.getIsAnonymous()) {
                        commentDto.setAuthorName("Anonymous");
                    }
                    return commentDto;
                })
                .collect(Collectors.toList());
    }
}
