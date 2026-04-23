package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.common.config.DateTimeFormatConfig;
import ru.practicum.event.enums.StateActionAdmin;
import ru.practicum.location.dto.LocationDto;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventAdminDto {

    @Size(min = 20, max = 2000, message = "Аннотация должна содержать от 20 до 2000 символов")
    private String annotation;

    private Long category;

    @Size(min = 20, max = 7000, message = "Описание должно содержать от 20 до 7000 символов")
    private String description;

    @JsonFormat(pattern = DateTimeFormatConfig.PATTERN)
    private LocalDateTime eventDate;

    private LocationDto location;
    private Boolean paid;

    @PositiveOrZero(message = "Лимит участников не может быть отрицательным")
    private Integer participantLimit;

    private Boolean requestModeration;
    private StateActionAdmin stateAction;

    @Size(min = 3, max = 120, message = "Заголовок должен содержать от 3 до 120 символов")
    private String title;
}