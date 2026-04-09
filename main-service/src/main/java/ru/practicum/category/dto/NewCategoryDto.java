package ru.practicum.category.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewCategoryDto {

    @NotBlank
    private String name;
}
