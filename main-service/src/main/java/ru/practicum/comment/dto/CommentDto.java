package ru.practicum.comment.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {
    private Long id;
    private String text;
    private Long authorId;
    private String authorName;
    private Long eventId;
    private String eventTitle;
    private LocalDateTime created;
    private LocalDateTime updated;
}
