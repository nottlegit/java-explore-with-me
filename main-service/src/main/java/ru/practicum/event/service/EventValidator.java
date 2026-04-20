package ru.practicum.event.service;

import org.springframework.stereotype.Component;
import ru.practicum.common.exception.BadRequestException;
import ru.practicum.event.dto.NewEventDto;

import java.time.LocalDateTime;

@Component
public class EventValidator {
    public static final int ANNOTATION_MIN_LENGTH = 20;
    public static final int ANNOTATION_MAX_LENGTH = 2000;
    public static final int DESCRIPTION_MIN_LENGTH = 20;
    public static final int DESCRIPTION_MAX_LENGTH = 7000;
    public static final int TITLE_MIN_LENGTH = 3;
    public static final int TITLE_MAX_LENGTH = 120;

    public static final int USER_EVENT_DATE_MIN_HOURS_AHEAD = 2;
    public static final int ADMIN_EVENT_DATE_MIN_HOURS_AHEAD = 1;

    public void validateUserEventDate(LocalDateTime eventDate) {
        validateEventDate(eventDate, USER_EVENT_DATE_MIN_HOURS_AHEAD);
    }

    public void validateAdminEventDate(LocalDateTime eventDate) {
        validateEventDate(eventDate, ADMIN_EVENT_DATE_MIN_HOURS_AHEAD);
    }

    public boolean isValidPublishEventDate(LocalDateTime eventDate) {
        return eventDate != null && !eventDate.isBefore(LocalDateTime.now().plusHours(ADMIN_EVENT_DATE_MIN_HOURS_AHEAD));
    }

    public void validateAnnotation(String annotation) {
        validateStringLength(annotation, ANNOTATION_MIN_LENGTH, ANNOTATION_MAX_LENGTH, "annotation");
    }

    public void validateDescription(String description) {
        validateStringLength(description, DESCRIPTION_MIN_LENGTH, DESCRIPTION_MAX_LENGTH, "description");
    }

    public void validateTitle(String title) {
        validateStringLength(title, TITLE_MIN_LENGTH, TITLE_MAX_LENGTH, "title");
    }

    public void validateNewEvent(NewEventDto newEventDto) {
        if (newEventDto == null) {
            return;
        }
        validateUserEventDate(newEventDto.getEventDate());
        validateAnnotation(newEventDto.getAnnotation());
        validateDescription(newEventDto.getDescription());
        validateTitle(newEventDto.getTitle());
    }

    private void validateEventDate(LocalDateTime eventDate, int hoursFromNow) {
        if (eventDate == null) {
            return;
        }
        if (eventDate.isBefore(LocalDateTime.now().plusHours(hoursFromNow))) {
            throw new BadRequestException("В поле eventDate ошибка: должно содержать дату, которая еще не наступила.");
        }
    }

    private void validateStringLength(String value, int min, int max, String field) {
        if (value == null) {
            return;
        }
        int length = value.trim().length();
        if (length < min || length > max) {
            throw new BadRequestException("Поле " + field + ". Ошибка: некорректная длина");
        }
    }

    public void validateRangeParams(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new BadRequestException("Поле rangeStart. Ошибка: начало диапазона должно быть раньше конца");
        }
    }

    public int normalizeParticipantLimit(Integer participantLimit) {
        return participantLimit == null ? 0 : participantLimit;
    }

    public void validateParticipantLimit(Integer participantLimit) {
        if (participantLimit != null && participantLimit < 0) {
            throw new BadRequestException("Лимит участников не может быть отрицательным");
        }
    }
}
