package ru.practicum.event.service;

import org.springframework.stereotype.Component;
import ru.practicum.common.exception.BadRequestException;
import ru.practicum.common.exception.ConflictException;

import java.time.LocalDateTime;

@Component
public class EventValidator {

    private static final int USER_EVENT_DATE_MIN_HOURS_AHEAD = 2;
    private static final int ADMIN_EVENT_DATE_MIN_HOURS_AHEAD = 1;

    public void validateNewEventDate(LocalDateTime eventDate) {
        if (eventDate == null) {
            return;
        }
        LocalDateTime minDate = LocalDateTime.now().plusHours(USER_EVENT_DATE_MIN_HOURS_AHEAD);
        if (eventDate.isBefore(minDate)) {
            throw new BadRequestException(
                    String.format("Дата события должна быть не ранее чем через %d часа(ов) от текущего момента",
                            USER_EVENT_DATE_MIN_HOURS_AHEAD)
            );
        }
    }

    public void validateUserEventDate(LocalDateTime eventDate) {
        if (eventDate == null) {
            return;
        }
        LocalDateTime minDate = LocalDateTime.now().plusHours(USER_EVENT_DATE_MIN_HOURS_AHEAD);
        if (eventDate.isBefore(minDate)) {
            throw new BadRequestException(
                    String.format("Дата события должна быть не ранее чем через %d часа(ов) от текущего момента",
                            USER_EVENT_DATE_MIN_HOURS_AHEAD)
            );
        }
    }

    public void validateAdminEventDate(LocalDateTime eventDate) {
        if (eventDate == null) {
            return;
        }
        LocalDateTime minDate = LocalDateTime.now().plusHours(ADMIN_EVENT_DATE_MIN_HOURS_AHEAD);
        if (eventDate.isBefore(minDate)) {
            throw new BadRequestException(
                    String.format("Дата события должна быть не ранее чем через %d часа(ов) от текущего момента",
                            ADMIN_EVENT_DATE_MIN_HOURS_AHEAD)
            );
        }
    }

    public boolean isValidPublishEventDate(LocalDateTime eventDate) {
        if (eventDate == null) {
            return false;
        }
        return !eventDate.isBefore(LocalDateTime.now().plusHours(ADMIN_EVENT_DATE_MIN_HOURS_AHEAD));
    }

    public void validateRangeParams(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new BadRequestException("Начало диапазона должно быть раньше конца");
        }
    }

    public int normalizeParticipantLimit(Integer participantLimit) {
        return participantLimit == null ? 0 : participantLimit;
    }
}