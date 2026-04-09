package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.common.config.DateTimeFormatConfig;
import ru.practicum.common.enums.EventState;
import ru.practicum.location.dto.LocationDto;
import ru.practicum.user.dto.UserShortDto;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDto {
    private Long id;
    private String annotation;
    private CategoryDto category;
    private Integer confirmedRequests;

    @JsonFormat(pattern = DateTimeFormatConfig.PATTERN)
    private LocalDateTime createdOn;
    private String description;

    @JsonFormat(pattern = DateTimeFormatConfig.PATTERN)
    private LocalDateTime eventDate;

    private UserShortDto initiator;
    private LocationDto location;
    private Boolean paid;
    private Integer participantLimit;

    @JsonFormat(pattern = DateTimeFormatConfig.PATTERN)
    private LocalDateTime publishedOn;

    private Boolean requiresModeration;
    private EventState state;
    private String title;
    private Long views;
}
