package ru.practicum.comment.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.service.CommentService;

import java.util.Collection;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/events/{eventId}/comments")
public class PublicCommentController {
    private final CommentService commentService;

    @GetMapping
    public Collection<CommentDto> getComments(@Positive @PathVariable Long eventId,
                                              @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                              @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Публичный запрос комментариев для события {} (from={}, size={})", eventId, from, size);
        return commentService.getComments(eventId, from, size);
    }

    @GetMapping("/{commentId}")
    public CommentDto getComment(@Positive @PathVariable Long commentId) {
        log.info("Публичный запрос комментария {}", commentId);
        return commentService.getComment(commentId);
    }
}
