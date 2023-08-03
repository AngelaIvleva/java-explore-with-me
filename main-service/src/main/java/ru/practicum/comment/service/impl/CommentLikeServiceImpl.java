package ru.practicum.comment.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.enums.LikeType;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.model.Like;
import ru.practicum.comment.repository.CommentLikeRepository;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.comment.service.CommentLikeService;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.util.ObjectCheckExistence;

import javax.transaction.Transactional;

import static ru.practicum.comment.enums.CommentStatus.PUBLISHED;
import static ru.practicum.comment.mapper.CommentMapper.COMMENT_MAPPER;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentLikeServiceImpl implements CommentLikeService {

    private final CommentLikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final ObjectCheckExistence checkExistence;

    @Transactional
    @Override
    public CommentDto addLikeToComment(Long commentId, Long userId, LikeType type) {
        User user = checkExistence.checkUser(userId);
        Comment comment = commentRepository.findByIdAndStatus(commentId, PUBLISHED)
                .orElseThrow(() -> new ConflictException("Поставить like/dislike можно только опубликованному комментарию"));
        Like like = likeRepository.findByCommentIdAndUserId(commentId, userId);
        if (like == null) {
            Like newLike = Like.builder()
                    .comment(comment)
                    .user(user)
                    .likeType(type)
                    .build();
            likeRepository.save(newLike);
        } else {
            if (like.getLikeType() == type) {
                throw new ConflictException("Нельзя поставить повторный like/dislike одному комментарию");
            } else {
                like.setLikeType(type);
                likeRepository.save(like);

                if (type == LikeType.LIKE) {
                    comment.setDislikes(comment.getDislikes() - 1);
                    comment.setLikes(comment.getLikes() + 1);
                } else {
                    comment.setDislikes(comment.getDislikes() + 1);
                    comment.setLikes(comment.getLikes() - 1);
                }
            }
        }
        log.info("Пользователь {} поставил {} комментарию {}", userId, type, commentId);
        return COMMENT_MAPPER.toCommentDto(commentRepository.save(comment));
    }

    @Transactional
    @Override
    public void removeLikeFromComment(Long commentId, Long userId) {
        Comment comment = checkExistence.checkComment(commentId);
        checkExistence.checkUser(userId);

        Like like = likeRepository.findByCommentIdAndUserId(commentId, userId);
        if (like == null) throw new NotFoundException("Лайк не найден");

        if (like.getLikeType() == LikeType.LIKE) {
            comment.setLikes(comment.getLikes() - 1);
        } else {
            comment.setDislikes(comment.getDislikes() - 1);
        }

        likeRepository.delete(like);
        commentRepository.save(comment);
        log.info("Пользователь {} удалил свой like/dislike к комментарию {}", userId, commentId);
    }
}
