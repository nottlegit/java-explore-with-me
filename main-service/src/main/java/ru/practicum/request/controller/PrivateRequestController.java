package ru.practicum.request.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;

import java.util.Collection;

@Validated
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/users/{userId}")
public class PrivateRequestController {
    private final RequestService requestService;

    @GetMapping("/requests")
    public Collection<ParticipationRequestDto> getUserRequests(@PathVariable Long userId) {
        log.info("Получен запрос на список заявок пользователя {}", userId);
        return requestService.getUserRequests(userId);
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable Long userId,
                                                 @RequestParam(name = "eventId") Long eventId) {
        log.info("Получен запрос на создание заявки пользователем {} для события {}", userId, eventId);
        return requestService.createRequest(userId, eventId);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable Long userId,
                                                 @PathVariable Long requestId) {
        log.info("Получен запрос на отмену заявки {} пользователем {}", requestId, userId);
        return requestService.cancelRequest(userId, requestId);
    }

    @GetMapping("/events/{eventId}/requests")
    public Collection<ParticipationRequestDto> getEventRequests(@PathVariable Long userId,
                                                                @PathVariable Long eventId) {
        log.info("Получен запрос на список заявок события {} от пользователя {}", eventId, userId);
        return requestService.getEventRequests(userId, eventId);
    }

    @PatchMapping("/events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestStatuses(@PathVariable Long userId,
                                                                @PathVariable Long eventId,
                                                                @Valid @RequestBody EventRequestStatusUpdateRequest updateRequest) {
        return requestService.updateRequestStatuses(userId, eventId, updateRequest);
    }
}
