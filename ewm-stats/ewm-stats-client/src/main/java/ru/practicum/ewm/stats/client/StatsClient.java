package ru.practicum.ewm.stats.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.StatsDateTimeFormat;
import ru.practicum.ewm.stats.dto.ViewStatsDto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

        ResponseEntity<EndpointHitDto> response = restTemplate.postForEntity(
                serverUrl + "/hit",
                request,
                EndpointHitDto.class
        );

        return response.getBody();
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        String encodedStart = URLEncoder.encode(start.format(FORMATTER), StandardCharsets.UTF_8);
        String encodedEnd = URLEncoder.encode(end.format(FORMATTER), StandardCharsets.UTF_8);

        StringBuilder urlBuilder = new StringBuilder(serverUrl)
                .append("/stats?start=").append(encodedStart)
                .append("&end=").append(encodedEnd);

        if (uris != null && !uris.isEmpty()) {
            String encodedUris = uris.stream()
                    .map(uri -> URLEncoder.encode(uri, StandardCharsets.UTF_8))
                    .collect(Collectors.joining(","));
            urlBuilder.append("&uris=").append(encodedUris);
        }

        urlBuilder.append("&unique=").append(unique);

        ResponseEntity<ViewStatsDto[]> response =
                restTemplate.getForEntity(urlBuilder.toString(), ViewStatsDto[].class);

        ViewStatsDto[] body = response.getBody();
        return body != null ? Arrays.asList(body) : List.of();
    }
}
