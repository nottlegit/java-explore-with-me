package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.exception.AlreadyExistsException;
import ru.practicum.common.exception.ConflictException;
import ru.practicum.common.exception.NotFoundException;
import ru.practicum.event.enums.EventState;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.enums.ParticipationRequestStatus;
import ru.practicum.request.mapper.ParticipationRequestMapper;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.repository.ParticipationRequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RequestService {
    private final ParticipationRequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserService userService;

    public Collection<ParticipationRequestDto> getUserRequests(Long userId) {
        log.info("Получение заявок пользователя {}", userId);
        userService.getUserOrThrow(userId);
        List<ParticipationRequestDto> requests = ParticipationRequestMapper.toDtoList(
                requestRepository.findAllByRequesterIdOrderByCreatedDesc(userId));
        log.debug("Найдено {} заявок пользователя {}", requests.size(), userId);
        return requests;
    }

    public Collection<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        log.info("Получение заявок на событие {} владельца {}", eventId, userId);
        validateEventOwner(userId, eventId);
        List<ParticipationRequestDto> requests = ParticipationRequestMapper.toDtoList(
                requestRepository.findAllByEventIdOrderByCreatedAsc(eventId));
        log.debug("Найдено {} заявок на событие {}", requests.size(), eventId);
        return requests;
    }

    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        log.info("Создание заявки: пользовательId={}, событиеId={}", userId, eventId);
        User requester = userService.getUserOrThrow(userId);
        Event event = getEventOrThrow(eventId);
        validateCanCreateRequest(userId, event);

        if (requestRepository.findByRequesterIdAndEventId(userId, eventId).isPresent()) {
            throw new AlreadyExistsException("Заявка уже существует");
        }

        ParticipationRequestStatus status = isAutoConfirmed(event)
                ? ParticipationRequestStatus.CONFIRMED
                : ParticipationRequestStatus.PENDING;

        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(requester)
                .status(status)
                .build();

        ParticipationRequest saved = requestRepository.save(request);
        log.debug("Заявка {} создана со статусом {}", saved.getId(), saved.getStatus());
        return ParticipationRequestMapper.toDto(saved);
    }

    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Отмена заявки {} пользователем {}", requestId, userId);
        userService.getUserOrThrow(userId);
        ParticipationRequest request = requestRepository.findById(requestId)
                .filter(item -> item.getRequester() != null && item.getRequester().getId().equals(userId))
                .orElseThrow(() -> new NotFoundException("Заявка с id=" + requestId + " не найдена"));

        request.setStatus(ParticipationRequestStatus.CANCELED);
        ParticipationRequest saved = requestRepository.save(request);
        log.debug("Заявка {} переведена в статус {}", saved.getId(), saved.getStatus());
        return ParticipationRequestMapper.toDto(saved);
    }

    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatuses(Long userId,
                                                                Long eventId,
                                                                EventRequestStatusUpdateRequest updateRequest) {
        validateEventOwner(userId, eventId);

        if (updateRequest == null || updateRequest.getStatus() == null || updateRequest.getRequestIds() == null) {
            throw new ConflictException("Необходимо указать идентификаторы заявок и статус");
        }

        if (updateRequest.getRequestIds().isEmpty()) {
            return EventRequestStatusUpdateResult.builder()
                    .confirmedRequests(List.of())
                    .rejectedRequests(List.of())
                    .build();
        }

        Event event = getEventOrThrow(eventId);
        Map<Long, ParticipationRequest> requestById = requestRepository.findAllByIdInAndEventId(updateRequest.getRequestIds(), eventId)
                .stream()
                .collect(Collectors.toMap(ParticipationRequest::getId, request -> request, (left, right) -> left, LinkedHashMap::new));

        List<ParticipationRequest> requests = new ArrayList<>();
        for (Long requestId : updateRequest.getRequestIds()) {
            ParticipationRequest request = requestById.get(requestId);
            if (request == null) {
                throw new NotFoundException("Заявка с id=" + requestId + " не найдена");
            }
            requests.add(request);
        }

        if (updateRequest.getStatus() == ParticipationRequestStatus.CONFIRMED) {
            return confirmRequests(event, requests);
        }

        if (updateRequest.getStatus() == ParticipationRequestStatus.REJECTED) {
            return rejectRequests(requests);
        }

        throw new ConflictException("Неподдерживаемый статус заявки");
    }

    public long countConfirmedRequestsByEventId(Long eventId) {
        long count = requestRepository.countByEventIdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);
        log.trace("Количество подтвержденных заявок для события {} -> {}", eventId, count);
        return count;
    }

    public Map<Long, Long> countConfirmedRequestsByEventIds(Collection<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, Long> counts = requestRepository.countByEventIdsAndStatus(eventIds, ParticipationRequestStatus.CONFIRMED)
                .stream()
                .collect(Collectors.toMap(
                        item -> (Long) item[0],
                        item -> (Long) item[1],
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
        log.trace("Подсчитаны подтвержденные заявки для {} событий", eventIds.size());
        return counts;
    }

    private EventRequestStatusUpdateResult confirmRequests(Event event, List<ParticipationRequest> requests) {
        log.debug("Подтверждение заявок для события {}, размерПакета={}", event.getId(), requests.size());
        long limit = event.getParticipantLimit();
        long confirmedCount = countConfirmedRequestsByEventId(event.getId());

        if (limit > 0 && confirmedCount >= limit) {
            throw new ConflictException("Лимит участников достигнут");
        }

        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        for (ParticipationRequest request : requests) {
            if (request.getStatus() != ParticipationRequestStatus.PENDING) {
                throw new ConflictException("Заявка должна иметь статус PENDING");
            }

            if (limit > 0 && confirmedCount >= limit) {
                request.setStatus(ParticipationRequestStatus.REJECTED);
                rejected.add(ParticipationRequestMapper.toDto(request));
                continue;
            }

            request.setStatus(ParticipationRequestStatus.CONFIRMED);
            confirmedCount++;
            confirmed.add(ParticipationRequestMapper.toDto(request));
        }

        requestRepository.saveAll(requests);
        log.debug("Итог подтверждения для события {}: подтверждено={}, отклонено={}",
                event.getId(),
                confirmed.size(),
                rejected.size());
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmed)
                .rejectedRequests(rejected)
                .build();
    }

    private EventRequestStatusUpdateResult rejectRequests(List<ParticipationRequest> requests) {
        log.debug("Отклонение заявок, размерПакета={}", requests.size());
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        for (ParticipationRequest request : requests) {
            if (request.getStatus() != ParticipationRequestStatus.PENDING) {
                throw new ConflictException("Заявка должна иметь статус PENDING");
            }

            request.setStatus(ParticipationRequestStatus.REJECTED);
            rejected.add(ParticipationRequestMapper.toDto(request));
        }

        requestRepository.saveAll(requests);
        log.debug("Отклонено {} заявок", rejected.size());
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(List.of())
                .rejectedRequests(rejected)
                .build();
    }

    private void validateCanCreateRequest(Long userId, Event event) {
        if (event.getInitiator() != null && event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Инициатор не может создать заявку на собственное событие");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Событие должно быть опубликовано");
        }

        if (requestRepository.findByRequesterIdAndEventId(userId, event.getId()).isPresent()) {
            throw new AlreadyExistsException("Заявка уже существует");
        }

        if (event.getParticipantLimit() > 0
                && countConfirmedRequestsByEventId(event.getId()) >= event.getParticipantLimit()) {
            throw new ConflictException("Лимит участников достигнут");
        }
    }

    private boolean isAutoConfirmed(Event event) {
        return Boolean.FALSE.equals(event.getRequestModeration()) || event.getParticipantLimit() == 0;
    }

    private Event validateEventOwner(Long userId, Long eventId) {
        Event event = getEventOrThrow(eventId);
        if (event.getInitiator() == null || !event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено");
        }
        return event;
    }

    private Event getEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
    }
}
