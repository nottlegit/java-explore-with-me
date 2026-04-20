package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.event.dto.EventDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.model.Event;
import ru.practicum.ewm.stats.client.StatsClient;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;
import ru.practicum.request.service.RequestService;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EventViewService {
    private final StatsClient statsClient;
    private final RequestService requestService;

    public void registerHit(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();
        EndpointHitDto hitDto = EndpointHitDto.builder()
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.now())
                .build();
        statsClient.sendHit(hitDto);
    }

    public void enrichEventDtos(List<EventDto> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        Map<Long, Long> viewsByEventId = getViewsByEventIds(
                events.stream().map(EventDto::getId).toList(),
                firstCreatedOn(events),
                LocalDateTime.now());
        Map<Long, Long> confirmedByEventId = getConfirmedRequestsByEventIds(
                events.stream().map(EventDto::getId).filter(Objects::nonNull).toList());

        for (EventDto dto : events) {
            long views = viewsByEventId.getOrDefault(dto.getId(), 0L);
            long confirmedRequests = confirmedByEventId.getOrDefault(dto.getId(), 0L);
            dto.setViews(views);
            dto.setConfirmedRequests((int) confirmedRequests);
        }
    }

    public List<EventShortDto> enrichShortDtos(List<EventShortDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return dtos;
        }

        List<Long> eventIds = dtos.stream().map(EventShortDto::getId).toList();
        Map<Long, Long> viewsByEventId = getViewsByEventIds(eventIds, LocalDateTime.now().minusYears(1), LocalDateTime.now());
        Map<Long, Long> confirmedByEventId = getConfirmedRequestsByEventIds(
                eventIds.stream().filter(Objects::nonNull).toList());

        for (EventShortDto dto : dtos) {
            long views = viewsByEventId.getOrDefault(dto.getId(), 0L);
            long confirmedRequests = confirmedByEventId.getOrDefault(dto.getId(), 0L);
            dto.setViews(views);
            dto.setConfirmedRequests((int) confirmedRequests);
        }
        return dtos;
    }

    public void enrichEventDto(EventDto dto, LocalDateTime createdOn) {
        if (dto == null || dto.getId() == null) {
            return;
        }
        Map<Long, Long> viewsByEventId = getViewsByEventIds(List.of(dto.getId()),
                createdOn == null ? LocalDateTime.now() : createdOn,
                LocalDateTime.now());
        Map<Long, Long> confirmedByEventId = getConfirmedRequestsByEventIds(List.of(dto.getId()));

        dto.setViews(viewsByEventId.getOrDefault(dto.getId(), 0L));
        dto.setConfirmedRequests(confirmedByEventId.getOrDefault(dto.getId(), 0L).intValue());
    }

    public Map<Long, Long> getViewsByEventIds(Collection<Long> eventIds, LocalDateTime startTime, LocalDateTime endTime) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> ids = eventIds.stream().filter(Objects::nonNull).toList();
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> uris = ids.stream().map(id -> "/events/" + id).toList();
        List<ViewStatsDto> stats = getStats(startTime, endTime, uris);

        Map<Long, Long> result = new LinkedHashMap<>();
        for (ViewStatsDto stat : stats) {
            Long eventId = extractEventId(stat.getUri());
            if (eventId != null) {
                result.put(eventId, stat.getHits());
            }
        }
        return result;
    }

    public Map<Long, Long> getConfirmedRequestsByEventIds(Collection<Long> eventIds) {
        return requestService.countConfirmedRequestsByEventIds(eventIds);
    }

    public Map<Long, Long> getViewsByEvents(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> ids = events.stream().map(Event::getId).filter(Objects::nonNull).toList();
        LocalDateTime startTime = events.stream()
                .map(Event::getCreatedOn)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
        return getViewsByEventIds(ids, startTime, LocalDateTime.now());
    }

    private List<ViewStatsDto> getStats(LocalDateTime startTime, LocalDateTime endTime, List<String> uris) {
        return statsClient.getStats(startTime, endTime, uris, true);
    }

    private Long extractEventId(String uri) {
        if (uri == null || !uri.startsWith("/events/")) {
            return null;
        }
        String idPart = uri.substring("/events/".length());
        try {
            return Long.parseLong(idPart);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private LocalDateTime firstCreatedOn(List<EventDto> events) {
        return events.stream()
                .map(EventDto::getCreatedOn)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
    }
}
