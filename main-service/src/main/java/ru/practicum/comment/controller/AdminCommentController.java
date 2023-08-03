package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentFullDto;
import ru.practicum.comment.dto.UpdateCommentAdminDto;
import ru.practicum.comment.enums.CommentStatus;
import ru.practicum.comment.service.AdminCommentService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/comments")
public class AdminCommentController {

    private final AdminCommentService commentService;

    @GetMapping("/{commentId}")
    public CommentFullDto getByCommentId(@PathVariable("commentId") Long commentId) {
        return commentService.getByIdByAdmin(commentId);
    }

    @GetMapping("/user/{userId}")
    public List<CommentFullDto> getByUserId(@PathVariable("userId") Long userId,
                                            @RequestParam(required = false) CommentStatus status,
                                            @PositiveOrZero @RequestParam(value = "from", defaultValue = "0",
                                                    required = false) Integer from,
                                            @Positive @RequestParam(value = "size", defaultValue = "10",
                                                    required = false) Integer size) {
        return commentService.getByUserIdByAdmin(userId, status, PageRequest.of(from / size, size));
    }

    @GetMapping("/event/{eventId}")
    public List<CommentFullDto> getByEventId(@PathVariable("eventId") Long eventId,
                                             @RequestParam(required = false) CommentStatus status,
                                             @PositiveOrZero @RequestParam(value = "from", defaultValue = "0",
                                                     required = false) Integer from,
                                             @Positive @RequestParam(value = "size", defaultValue = "10",
                                                     required = false) Integer size) {
        return commentService.getByEventIdByAdmin(eventId, status, PageRequest.of(from / size, size));
    }

    @PatchMapping("/{commentId}")
    public CommentFullDto updateComment(@PathVariable("commentId") Long commentId,
                                        @Valid @RequestBody UpdateCommentAdminDto updateCommentByAdmin) {
        return commentService.updateCommentByAdmin(commentId, updateCommentByAdmin);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("commentId") Long commentId) {
        commentService.deleteByIdByAdmin(commentId);
    }

}
