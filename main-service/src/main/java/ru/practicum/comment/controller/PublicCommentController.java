package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.service.PublicCommentService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class PublicCommentController {

    private final PublicCommentService commentService;

    @GetMapping("/{commentId}")
    public CommentDto get(@PathVariable("commentId") Long commentId) {
        return commentService.getByCommentIdByPublic(commentId);
    }

    @GetMapping("/event/{eventId}")
    public List<CommentDto> getByEventId(@PathVariable("eventId") Long eventId,
                                         @PositiveOrZero @RequestParam(value = "from", defaultValue = "0",
                                                 required = false) Integer from,
                                         @Positive @RequestParam(value = "size", defaultValue = "10",
                                                 required = false) Integer size) {
        return commentService.getByEventIdByPublic(eventId, PageRequest.of(from / size, size));
    }

    @GetMapping("/user/{userId}")
    public List<CommentDto> getByUserId(@PathVariable("userId") Long userId,
                                        @PositiveOrZero @RequestParam(value = "from", defaultValue = "0",
                                                required = false) Integer from,
                                        @Positive @RequestParam(value = "size", defaultValue = "10",
                                                required = false) Integer size) {
        return commentService.getByUserIdByPublic(userId, PageRequest.of(from / size, size));
    }

    @GetMapping("/replies/{commentId}")
    public List<CommentDto> getReplies(@PathVariable("commentId") Long commentId,
                                       @PositiveOrZero @RequestParam(value = "from", defaultValue = "0",
                                               required = false) Integer from,
                                       @Positive @RequestParam(value = "size", defaultValue = "10",
                                               required = false) Integer size) {
        return commentService.getReplies(commentId,
                PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "created")));
    }
}
