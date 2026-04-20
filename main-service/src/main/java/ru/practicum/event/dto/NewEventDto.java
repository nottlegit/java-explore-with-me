package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.common.config.DateTimeFormatConfig;
import ru.practicum.location.dto.LocationDto;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {
    @NotBlank
    private String annotation;

    private Long category;

    @NotBlank
    private String description;

    @JsonFormat(pattern = DateTimeFormatConfig.PATTERN)
    private LocalDateTime eventDate;
    private LocationDto location;
    private Boolean paid;

    @PositiveOrZero
    private Integer participantLimit;
    private Boolean requestModeration;

    @NotBlank
    private String title;
}
