package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.common.exception.ConflictException;
import ru.practicum.common.exception.NotFoundException;
import ru.practicum.event.enums.EventState;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Collection<CommentDto> getComments(Long eventId, Integer from, Integer size) {
        log.info("Получение комментариев для события {} (from={}, size={})", eventId, from, size);
        Event event = getEventOrThrow(eventId);

        return commentRepository.findByEvent(event, PageRequest.of(from / size, size))
                .stream()
                .map(CommentMapper::toCommentDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public CommentDto getComment(Long commentId) {
        log.info("Получение комментария {}", commentId);
        Comment comment = getCommentOrThrow(commentId);

        return CommentMapper.toCommentDto(comment);
    }

    @Transactional
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto dto) {
        log.info("Создание комментария для события {} от пользователя {}", eventId, userId);
        User author = getUserOrThrow(userId);
        Event event = getEventOrThrow(eventId);

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Событие должно быть опубликовано");
        }

        Comment comment = CommentMapper.toComment(dto, author, event, LocalDateTime.now());

        comment = commentRepository.save(comment);
        log.info("Создан комментарий {} пользователем {} к событию {}", comment.getId(), userId, event.getId());
        return CommentMapper.toCommentDto(comment);
    }

    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto dto) {
        log.info("Обновление комментария {} пользователем {}", commentId, userId);
        User author = getUserOrThrow(userId);
        Comment comment = getCommentOrThrow(commentId);

        if (!comment.getAuthor().getId().equals(author.getId())) {
            throw new ConflictException("Пользователь не является автором комментария");
        }

        CommentMapper.updateComment(comment, dto, LocalDateTime.now());

        comment = commentRepository.save(comment);

        log.info("Обновлен комментарий {} пользователем {}", commentId, userId);
        return CommentMapper.toCommentDto(comment);
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        log.info("Удаление комментария {} пользователем {}", commentId, userId);
        Comment comment = getCommentOrThrow(commentId);
        User author = getUserOrThrow(userId);

        if (!comment.getAuthor().getId().equals(author.getId())) {
            throw new ConflictException("Пользователь не является автором комментария");
        }

        commentRepository.delete(comment);
        log.info("Комментарий {} удален пользователем {}", commentId, userId);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        log.info("Администратор удаляет комментарий {}", commentId);
        Comment comment = getCommentOrThrow(commentId);
        commentRepository.delete(comment);
        log.info("Комментарий {} удален администратором", commentId);
    }

    private Event getEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие не найдено"));

    }

    private Comment getCommentOrThrow(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("Комментарий не найден"));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }
}
