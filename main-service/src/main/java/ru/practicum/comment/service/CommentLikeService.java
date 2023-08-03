package ru.practicum.comment.service;

import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.enums.LikeType;

public interface CommentLikeService {

    CommentDto addLikeToComment(Long commentId, Long userId, LikeType type);

    void removeLikeFromComment(Long commentId, Long userId);
}
