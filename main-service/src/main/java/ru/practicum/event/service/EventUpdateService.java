package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.common.exception.ConflictException;
import ru.practicum.common.exception.NotFoundException;
import ru.practicum.event.dto.UpdateEventAdminDto;
import ru.practicum.event.dto.UpdateEventUserDto;
import ru.practicum.event.enums.EventState;
import ru.practicum.event.enums.StateActionAdmin;
import ru.practicum.event.enums.StateActionUser;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.location.dto.LocationDto;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EventUpdateService {
    private final CategoryRepository categoryRepository;
    private final EventValidator eventValidator;

    public void applyUserUpdate(Event event, UpdateEventUserDto request) {
        if (request == null) {
            return;
        }

        applyCommonFields(event,
                request.getAnnotation(),
                request.getDescription(),
                request.getTitle(),
                request.getCategory(),
                request.getLocation(),
                request.getPaid(),
                request.getParticipantLimit(),
                request.getRequestModeration(),
                request.getEventDate());

        if (request.getEventDate() != null) {
            eventValidator.validateUserEventDate(request.getEventDate());
        }

        applyUserStateTransition(event, request.getStateAction());
    }

    public void applyAdminUpdate(Event event, UpdateEventAdminDto request) {
        if (request == null) {
            return;
        }

        applyCommonFields(event,
                request.getAnnotation(),
                request.getDescription(),
                request.getTitle(),
                request.getCategory(),
                request.getLocation(),
                request.getPaid(),
                request.getParticipantLimit(),
                request.getRequestModeration(),
                request.getEventDate());

        if (request.getEventDate() != null) {
            eventValidator.validateAdminEventDate(request.getEventDate());
        }

        applyAdminStateTransition(event, request.getStateAction());
    }

    private void applyCommonFields(Event event,
                                   String annotation,
                                   String description,
                                   String title,
                                   Long categoryId,
                                   LocationDto location,
                                   Boolean paid,
                                   Integer participantLimit,
                                   Boolean requestModeration,
                                   LocalDateTime eventDate) {
        if (annotation != null) {
            event.setAnnotation(annotation.trim());
        }
        if (description != null) {
            event.setDescription(description.trim());
        }
        if (title != null) {
            event.setTitle(title.trim());
        }
        if (categoryId != null) {
            event.setCategory(getCategoryOrThrow(categoryId));
        }
        if (location != null) {
            event.setLocation(EventMapper.toLocation(location));
        }
        if (paid != null) {
            event.setPaid(paid);
        }
        if (participantLimit != null) {
            event.setParticipantLimit(participantLimit);
        }
        if (requestModeration != null) {
            event.setRequestModeration(requestModeration);
        }
        if (eventDate != null) {
            event.setEventDate(eventDate);
        }
    }

    private void applyUserStateTransition(Event event, StateActionUser stateAction) {
        if (stateAction == null) {
            return;
        }

        switch (stateAction) {
            case CANCEL_REVIEW -> event.setState(EventState.CANCELED);
            case SEND_TO_REVIEW -> event.setState(EventState.PENDING);
        }
    }

    private void applyAdminStateTransition(Event event, StateActionAdmin stateAction) {
        if (stateAction == null) {
            return;
        }

        switch (stateAction) {
            case PUBLISH_EVENT -> {
                if (event.getState() != EventState.PENDING) {
                    throw new ConflictException("Нельзя опубликовать событие, если оно не находится в статусе ожидания");
                }
                if (!eventValidator.isValidPublishEventDate(event.getEventDate())) {
                    throw new ConflictException("Дата события должна быть не раньше чем через час после публикации");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }
            case REJECT_EVENT -> {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Нельзя отклонить уже опубликованное событие");
                }
                event.setState(EventState.CANCELED);
            }
        }
    }

    private Category getCategoryOrThrow(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с id=" + catId + " не найдена"));
    }
}