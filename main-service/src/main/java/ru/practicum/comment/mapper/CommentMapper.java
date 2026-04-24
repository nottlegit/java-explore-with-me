package ru.practicum.comment.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.event.model.Event;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

@UtilityClass
public class CommentMapper {
    public Comment toComment(NewCommentDto dto, User author, Event event, LocalDateTime created) {
        if (dto == null) {
            return null;
        }

        return Comment.builder()
                .text(dto.getText())
                .author(author)
                .event(event)
                .created(created)
                .updated(null)
                .build();
    }

    public CommentDto toCommentDto(Comment comment) {
        if (comment == null) {
            return null;
        }

        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorId(comment.getAuthor().getId())
                .authorName(comment.getAuthor().getName())
                .eventId(comment.getEvent().getId())
                .eventTitle(comment.getEvent().getTitle())
                .created(comment.getCreated())
                .updated(comment.getUpdated())
                .build();
    }

    public void updateComment(Comment comment, UpdateCommentDto dto, LocalDateTime updated) {
        if (dto == null || comment == null) {
            return;
        }
        comment.setText(dto.getText());
        comment.setUpdated(updated);
    }
}
