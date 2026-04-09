package ru.practicum.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.config.DateTimeFormatConfig;
import ru.practicum.common.enums.EventState;
import ru.practicum.event.dto.EventDto;
import ru.practicum.event.dto.UpdateEventAdminDto;
import ru.practicum.event.service.EventService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/events")
public class AdminEventController {
    private final EventService eventService;

    @PatchMapping("/{eventId}")
    public EventDto updateEvent(@PathVariable Long eventId, @Valid @RequestBody UpdateEventAdminDto updateEventAdminDto) {
        return eventService.updateEvent(eventId, updateEventAdminRequest);
    }

    @GetMapping
    public Collection<EventDto> getEvents(
            @RequestParam(name = "users", required = false) List<Long> users,
            @RequestParam(name = "states", required = false) EventState states,
            @RequestParam(name = "categories", required = false) List<Long> categories,
            @RequestParam(name = "rangeStart", required = false)
                @DateTimeFormat(pattern = DateTimeFormatConfig.PATTERN) LocalDateTime rangeStart,
            @RequestParam(name = "rangeEnd", required = false)
                @DateTimeFormat(pattern = DateTimeFormatConfig.PATTERN) LocalDateTime rangeEnd,
            @RequestParam(required = false, defaultValue = "0") Integer from,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        return eventService.getEvents(users, states, categories, rangeStart, rangeEnd, from, size);
    }

}
