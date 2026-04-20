package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import ru.practicum.common.config.DateTimeFormatConfig;
import ru.practicum.event.enums.StateActionAdmin;
import ru.practicum.location.dto.LocationDto;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventAdminDto {
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
    private StateActionAdmin stateAction;
    private String title;
}
