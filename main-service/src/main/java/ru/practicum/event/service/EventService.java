package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.common.exception.ConflictException;
import ru.practicum.common.exception.NotFoundException;
import ru.practicum.event.dto.*;
import ru.practicum.event.enums.EventState;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;
    private final EventValidator eventValidator;
    private final EventUpdateService eventUpdateService;
    private final EventViewService eventViewService;

    @Transactional
    public EventDto createEvent(Long userId, NewEventDto newEventDto) {
        log.info("Создание события пользователем {}", userId);
        User initiator = userService.getUserOrThrow(userId);
        Category category = getCategoryOrThrow(newEventDto.getCategory());

        eventValidator.validateNewEventDate(newEventDto.getEventDate());

        Event event = EventMapper.toEvent(newEventDto, category, initiator);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        event.setRequestModeration(newEventDto.getRequestModeration() == null || newEventDto.getRequestModeration());
        event.setParticipantLimit(eventValidator.normalizeParticipantLimit(newEventDto.getParticipantLimit()));
        event.setPaid(Boolean.TRUE.equals(newEventDto.getPaid()));

        log.debug("Событие для пользователя {} подготовлено к сохранению", userId);
        return EventMapper.toEventDto(eventRepository.save(event));
    }

    @Transactional(readOnly = true)
    public Collection<EventShortDto> getUserEvents(Long userId, Integer from, Integer size) {
        log.info("Получение событий пользователя {} с параметрами from={}, size={}", userId, from, size);
        userService.getUserOrThrow(userId);

        Pageable pageable = buildPageable(from, size, Sort.by(Sort.Direction.DESC, "createdOn"));

        return eventRepository.findAllByInitiator_IdOrderByCreatedOnDesc(userId, pageable)
                .stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventDto getUserEvent(Long userId, Long eventId) {
        log.info("Получение события {} пользователя {}", eventId, userId);
        Event event = getEventOrThrow(eventId);
        ensureOwner(event, userId);
        return EventMapper.toEventDto(event);
    }

    @Transactional
    public EventDto updateUserEvent(Long userId, Long eventId, UpdateEventUserDto request) {
        log.info("Обновление события {} пользователем {}", eventId, userId);
        Event event = getEventOrThrow(eventId);
        ensureOwner(event, userId);

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Изменять можно только события в статусе ожидания или отмены");
        }

        eventUpdateService.applyUserUpdate(event, request);
        return EventMapper.toEventDto(eventRepository.save(event));
    }

    @Transactional(readOnly = true)
    public Collection<EventDto> getEvents(List<Long> users,
                                          List<EventState> states,
                                          List<Long> categories,
                                          LocalDateTime rangeStart,
                                          LocalDateTime rangeEnd,
                                          Integer from,
                                          Integer size) {
        log.info("Получение списка событий администратора. "
                        + "users={}, states={}, categories={}, rangeStart={}, rangeEnd={}, from={}, size={}",
                users, states, categories, rangeStart, rangeEnd, from, size);
        eventValidator.validateRangeParams(rangeStart, rangeEnd);

        Pageable pageable = buildPageable(from, size, Sort.by(Sort.Direction.DESC, "createdOn"));

        Page<Event> page = eventRepository.findEvents(
                isEmpty(users) ? null : users,
                isEmpty(states) ? null : states,
                isEmpty(categories) ? null : categories,
                rangeStart,
                rangeEnd,
                pageable
        );

        List<EventDto> events = page.stream()
                .map(EventMapper::toEventDto)
                .collect(Collectors.toList());

        eventViewService.enrichEventDtos(events);
        return events;
    }

    @Transactional
    public EventDto updateEvent(Long eventId, UpdateEventAdminDto request) {
        log.info("Административное обновление события {}", eventId);
        Event event = getEventOrThrow(eventId);
        eventUpdateService.applyAdminUpdate(event, request);
        return EventMapper.toEventDto(eventRepository.save(event));
    }

    @Transactional(readOnly = true)
    public Collection<EventShortDto> getPublicEvents(String text,
                                                     List<Long> categories,
                                                     Boolean paid,
                                                     LocalDateTime rangeStart,
                                                     LocalDateTime rangeEnd,
                                                     Boolean onlyAvailable,
                                                     String sort,
                                                     Integer from,
                                                     Integer size,
                                                     HttpServletRequest request) {
        log.info("Публичный поиск событий. "
                        + "text='{}', categories={}, paid={}, rangeStart={}, rangeEnd={}, "
                        + "onlyAvailable={}, sort={}, from={}, size={}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
        eventValidator.validateRangeParams(rangeStart, rangeEnd);
        eventViewService.registerHit(request);

        Pageable pageable = buildPageable(from, size, Sort.by(Sort.Direction.ASC, "eventDate"));

        Page<Event> page = eventRepository.findPublicEvents(
                text,
                isEmpty(categories) ? null : categories,
                paid,
                rangeStart,
                rangeEnd,
                Boolean.TRUE.equals(onlyAvailable),
                pageable
        );

        List<Event> events = page.getContent();

        if ("VIEWS".equalsIgnoreCase(sort)) {
            Map<Long, Long> viewsByEventId = eventViewService.getViewsByEvents(events);
            events = events.stream()
                    .sorted(Comparator.comparingLong((Event event) ->
                            viewsByEventId.getOrDefault(event.getId(), 0L)).reversed())
                    .collect(Collectors.toList());
        }

        List<EventShortDto> dtos = events.stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());

        return eventViewService.enrichShortDtos(dtos);
    }

    @Transactional(readOnly = true)
    public EventDto getPublicEvent(Long id, HttpServletRequest request) {
        log.info("Получение публичной информации о событии {}", id);
        Event event = getPublishedEventOrThrow(id);
        eventViewService.registerHit(request);
        EventDto dto = EventMapper.toEventDto(event);
        eventViewService.enrichEventDto(dto, event.getCreatedOn());
        return dto;
    }

    public boolean isExistingByCategoryId(Long catId) {
        return eventRepository.existsByCategory_Id(catId);
    }

    private Event getPublishedEventOrThrow(Long eventId) {
        Event event = getEventOrThrow(eventId);
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено");
        }
        return event;
    }

    private Event getEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
    }

    private Category getCategoryOrThrow(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с id=" + catId + " не найдена"));
    }

    private void ensureOwner(Event event, Long userId) {
        if (event.getInitiator() == null || !Objects.equals(event.getInitiator().getId(), userId)) {
            throw new NotFoundException("Событие с id=" + event.getId() + " не найдено");
        }
    }

    private Pageable buildPageable(Integer from, Integer size, Sort sort) {
        int limit = defaultLimit(size);
        int page = defaultOffset(from) / limit;

        return sort == null
                ? PageRequest.of(page, limit)
                : PageRequest.of(page, limit, sort);
    }

    private int defaultOffset(Integer from) {
        return from == null || from < 0 ? 0 : from;
    }

    private int defaultLimit(Integer size) {
        return size == null || size <= 0 ? 10 : size;
    }

    private boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
}