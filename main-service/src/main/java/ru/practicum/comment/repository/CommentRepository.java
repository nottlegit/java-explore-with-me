package ru.practicum.comment.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.comment.model.Comment;
import ru.practicum.event.model.Event;

import java.util.Collection;

public interface CommentRepository  extends JpaRepository<Comment, Long> {
    Collection<Comment> findByEvent(Event event, PageRequest pageRequest);
}
