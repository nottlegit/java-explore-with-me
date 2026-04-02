package ru.practicum.ewm.stats.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;
import ru.practicum.ewm.stats.server.mapper.StatsMapper;
import ru.practicum.ewm.stats.server.model.EndpointHit;
import ru.practicum.ewm.stats.server.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final StatsRepository statsRepository;

    public EndpointHitDto saveHit(EndpointHitDto endpointHitDto) {
        EndpointHit endpointHit = StatsMapper.toEntity(endpointHitDto);
        EndpointHit savedEndpointHit = statsRepository.save(endpointHit);
        return StatsMapper.toDto(savedEndpointHit);
    }

    public Collection<ViewStatsDto> getStats(
            LocalDateTime start,
            LocalDateTime end,
            List<String> uris,
            boolean unique) {
        boolean hasUris = uris != null && !uris.isEmpty();

        if (unique) {
            return hasUris
                    ? statsRepository.findUniqueStats(start, end, uris)
                    : statsRepository.findUniqueStats(start, end);
        }

        return hasUris
                ? statsRepository.findStats(start, end, uris)
                : statsRepository.findStats(start, end);
    }
}
