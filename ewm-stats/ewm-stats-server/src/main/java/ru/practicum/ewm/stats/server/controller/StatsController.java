package ru.practicum.ewm.stats.server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.StatsDateTimeFormat;
import ru.practicum.ewm.stats.dto.ViewStatsDto;
import ru.practicum.ewm.stats.server.service.StatsService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void saveHit(@Valid @RequestBody EndpointHitDto endpointHitDto) {
        statsService.saveHit(endpointHitDto);
    }

    @GetMapping("/stats")
    public Collection<ViewStatsDto> getStats(
            @RequestParam
            @DateTimeFormat(pattern = StatsDateTimeFormat.PATTERN)
            LocalDateTime start,
            @RequestParam
            @DateTimeFormat(pattern = StatsDateTimeFormat.PATTERN)
            LocalDateTime end,
            @RequestParam(required = false)
            List<String> uris,
            @RequestParam(defaultValue = "false")
            boolean unique
    ) {
        return statsService.getStats(start, end, uris, unique);
    }
}
