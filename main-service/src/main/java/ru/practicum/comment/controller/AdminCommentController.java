package ru.practicum.comment.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.service.CommentService;

import java.util.Collection;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/comments")
public class AdminCommentController {
    private final CommentService commentService;

    @GetMapping
    public Collection<CommentDto> getComments(@Positive @RequestParam Long eventId,
                                              @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                              @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Администратор запрашивает комментарии для события {} (from={}, size={})", eventId, from, size);
        return commentService.getComments(eventId, from, size);
    }

    @GetMapping("/{commentId}")
    public CommentDto getComment(@Positive @PathVariable Long commentId) {
        log.info("Администратор запрашивает комментарий {}", commentId);
        return commentService.getComment(commentId);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@Positive @PathVariable Long commentId) {
        log.info("Администратор удаляет комментарий {}", commentId);
        commentService.deleteComment(commentId);
    }
}
