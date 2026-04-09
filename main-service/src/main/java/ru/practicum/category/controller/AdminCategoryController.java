package ru.practicum.category.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.service.CategoryService;
import ru.practicum.common.exception.NotEmptyException;
import ru.practicum.event.service.EventService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
public class AdminCategoryController {
    private final CategoryService categoryService;
    private final EventService eventService;

    @PostMapping
    public CategoryDto createCategory(@Valid @RequestBody NewCategoryDto newCategoryDto) {
        return categoryService.createCategory(newCategoryDto);
    }

    @PatchMapping("/{catId}")
    public CategoryDto updateCategory(@PathVariable Long catId, @Valid @RequestBody CategoryDto categoryDto) {
        return categoryService.updateCategory(catId, categoryDto);
    }

    @DeleteMapping("/{catId}")
    public void deleteCategory(@PathVariable Long catId) {
        if (eventService.isExistingByCategoryId(catId)) {
            throw new NotEmptyException("Category has events");
        }
        categoryService.deleteCategory(catId);
    }
}
