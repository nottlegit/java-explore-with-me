package ru.practicum.ewm.stats.server.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.stats.dto.StatsDateTimeFormat;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stats")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EndpointHit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app")
    private String app;

    @Column(name = "uri")
    private String uri;

    @Column(name = "ip")
    private String ip;

    @Column(name = "hit_timestamp")
    @JsonFormat(pattern = StatsDateTimeFormat.PATTERN)
    private LocalDateTime hitTimestamp;
}
