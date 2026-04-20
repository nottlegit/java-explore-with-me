package ru.practicum.ewm.stats.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.StatsDateTimeFormat;
import ru.practicum.ewm.stats.dto.ViewStatsDto;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Component
public class StatsClient {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern(StatsDateTimeFormat.PATTERN);

    private final RestTemplate restTemplate;
    private final String serverUrl;
    private final String appName;

    public StatsClient(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${stats.server.url}") String serverUrl,
            @Value("${spring.application.name}") String appName
    ) {
        this.restTemplate = restTemplateBuilder.build();
        this.serverUrl = serverUrl;
        this.appName = appName;
    }

    public EndpointHitDto sendHit(EndpointHitDto hitDto) {
        EndpointHitDto request = EndpointHitDto.builder()
                .app(appName)
                .uri(hitDto.getUri())
                .ip(hitDto.getIp())
                .timestamp(hitDto.getTimestamp())
                .build();

        URI uri = UriComponentsBuilder.fromHttpUrl(serverUrl)
                .path("/hit")
                .build()
                .toUri();

        ResponseEntity<EndpointHitDto> response = restTemplate.postForEntity(
                uri,
                request,
                EndpointHitDto.class
        );

        return response.getBody();
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(serverUrl)
                .path("/stats")
                .queryParam("start", start.format(FORMATTER))
                .queryParam("end", end.format(FORMATTER))
                .queryParam("unique", unique);

        if (uris != null && !uris.isEmpty()) {
            uriBuilder.queryParam("uris", uris.toArray());
        }

        URI uri = uriBuilder.build().toUri();
        ResponseEntity<ViewStatsDto[]> response =
                restTemplate.getForEntity(uri, ViewStatsDto[].class);

        ViewStatsDto[] body = response.getBody();
        return body != null ? Arrays.asList(body) : List.of();
    }
}
