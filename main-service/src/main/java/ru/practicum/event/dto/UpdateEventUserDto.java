package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.common.config.DateTimeFormatConfig;
import ru.practicum.event.enums.StateActionUser;
import ru.practicum.location.dto.LocationDto;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventUserDto {
    private String annotation;
    private Long category;
    private String description;

    @JsonFormat(pattern = DateTimeFormatConfig.PATTERN)
    private LocalDateTime eventDate;
    private LocationDto location;
    private Boolean paid;

    @PositiveOrZero
    private Integer participantLimit;
    private Boolean requestModeration;
    private StateActionUser stateAction;
    private String title;
}
