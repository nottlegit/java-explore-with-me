package ru.practicum.event.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.common.config.DateTimeFormatConfig;
import ru.practicum.common.enums.StateActionAdmin;
import ru.practicum.location.model.Location;

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
    private Location location;
    private Boolean paid;
    private Long participantLimit;
    private Boolean requestModeration;
    private StateActionAdmin stateAction;
    private String title;
}
