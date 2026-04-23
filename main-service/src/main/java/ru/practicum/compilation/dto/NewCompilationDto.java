package ru.practicum.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewCompilationDto {
    @NotBlank
    @Size(min = 1, max = 50)
    private String title;
    private Boolean pinned;
    private List<Long> events;
}
