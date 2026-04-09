package ru.practicum.category.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.service.CategoryService;

import java.util.Collection;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class PublicCategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public Collection<CategoryDto> getCategories(@RequestParam(required = false, defaultValue = "0") Integer from,
                                                 @RequestParam(required = false, defaultValue = "10") Integer size) {
        return categoryService.getAllCategories(from, size);
    }

    @GetMapping("/{catId}")
    public CategoryDto getCategory(@PathVariable Long catId) {
        return categoryService.getCategory(catId);
    }
}
