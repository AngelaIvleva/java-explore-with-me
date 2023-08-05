package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.enums.CommentStatus;
import ru.practicum.comment.service.PrivateCommentService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/comments")
public class PrivateCommentController {

    private final PrivateCommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto post(@RequestParam Long eventId,
                           @PathVariable Long userId,
                           @RequestBody @Valid NewCommentDto commentDto) {
        return commentService.createCommentByUser(eventId, userId, commentDto);
    }

    @GetMapping
    public List<CommentDto> getAllOfUser(@PathVariable("userId") Long userId,
                                         @RequestParam(required = false) CommentStatus status,
                                         @PositiveOrZero @RequestParam(value = "from", defaultValue = "0",
                                                 required = false) Integer from,
                                         @Positive @RequestParam(value = "size", defaultValue = "10",
                                                 required = false) Integer size) {
        return commentService.getAllByUser(userId, status, PageRequest.of(from / size, size));
    }

    @PatchMapping("/{commentId}")
    public CommentDto update(@PathVariable("commentId") Long commentId,
                             @PathVariable("userId") Long userId,
                             @RequestBody @Valid NewCommentDto updateComment,
                             @RequestParam Long eventId) {
        return commentService.updateCommentByUser(commentId, eventId, userId, updateComment);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("commentId") Long commentId,
                       @PathVariable("userId") Long userId) {
        commentService.deleteCommentByUser(commentId, userId);
    }

}
