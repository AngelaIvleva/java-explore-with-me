package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.enums.LikeType;
import ru.practicum.comment.service.CommentLikeService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/comments/{commentId}/user/{userId}/likes")
public class CommentLikeController {

    private final CommentLikeService commentLikeService;

    @PostMapping("/like")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addLikeToComment(@PathVariable("commentId") Long commentId, @PathVariable("userId") Long userId) {
        return commentLikeService.addLikeToComment(commentId, userId, LikeType.LIKE);
    }

    @PostMapping("/dislike")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addDislikeToComment(@PathVariable("commentId") Long commentId, @PathVariable("userId") Long userId) {
        return commentLikeService.addLikeToComment(commentId, userId, LikeType.DISLIKE);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeLikeFromComment(@PathVariable Long commentId, @PathVariable("userId") Long userId) {
        commentLikeService.removeLikeFromComment(commentId, userId);
    }
}
