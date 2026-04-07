package ru.practicum.ewm.stats.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.stats.dto.ViewStatsDto;
import ru.practicum.ewm.stats.server.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface StatsRepository extends JpaRepository<EndpointHit, Long> {

    @Query("""
        SELECT NEW ru.practicum.ewm.stats.dto.ViewStatsDto(h.app, h.uri, count(h.id))
        FROM EndpointHit h
        where h.hitTimestamp BETWEEN :start AND :end
        GROUP BY h.app, h.uri
        ORDER BY count(h.id) DESC
    """)
    List<ViewStatsDto> findStats(LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT NEW ru.practicum.ewm.stats.dto.ViewStatsDto(h.app, h.uri, count(h.id))
        FROM EndpointHit h
        WHERE h.hitTimestamp BETWEEN :start AND :end
          AND h.uri IN :uris
        GROUP BY  h.app, h.uri
        ORDER BY count(h.id) DESC
    """)
    List<ViewStatsDto> findStats(LocalDateTime start, LocalDateTime end, Collection<String> uris);

    @Query("""
        SELECT NEW ru.practicum.ewm.stats.dto.ViewStatsDto(h.app, h.uri, count(DISTINCT h.ip))
        FROM EndpointHit h
        WHERE h.hitTimestamp BETWEEN :start AND :end
        GROUP BY h.app, h.uri
        ORDER BY count(DISTINCT h.ip) DESC
    """)
    List<ViewStatsDto> findUniqueStats(LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT NEW ru.practicum.ewm.stats.dto.ViewStatsDto(h.app, h.uri, count(DISTINCT h.ip))
        FROM EndpointHit h
        WHERE h.hitTimestamp BETWEEN :start AND :end
          AND h.uri IN :uris
        GROUP BY h.app, h.uri
        ORDER BY count(DISTINCT h.ip) DESC
    """)
    List<ViewStatsDto> findUniqueStats(LocalDateTime start, LocalDateTime end, Collection<String> uris);
}
