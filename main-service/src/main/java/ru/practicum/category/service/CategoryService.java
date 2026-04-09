package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.common.exception.AlreadyExistsException;
import ru.practicum.common.exception.NotFoundException;

import java.util.Collection;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new AlreadyExistsException("Category with name " + newCategoryDto.getName() + " already exists");
        }

        Category category = CategoryMapper.toCategory(newCategoryDto);
        Category savedCategory = categoryRepository.save(category);
        return CategoryMapper.toCategoryDto(savedCategory);
    }

    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        Category category = getCategoryOrThrow(catId);

        if (categoryRepository.existsByName(categoryDto.getName()) && !categoryDto.getName().equals(category.getName())) {
            throw new AlreadyExistsException("Category with name " + categoryDto.getName() + " already exists");
        }

        category = CategoryMapper.updateCategoryName(category, categoryDto.getName());
        Category updatedCategory = categoryRepository.save(category);
        return CategoryMapper.toCategoryDto(updatedCategory);
    }

    public CategoryDto getCategory(Long catId) {
        return CategoryMapper.toCategoryDto(getCategoryOrThrow(catId));
    }

    public Collection<CategoryDto> getAllCategories(Integer from, Integer size) {
        Collection<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .sorted(Comparator.comparing(Category::getId))
                .skip(from)
                .limit(size)
                .map(CategoryMapper::toCategoryDto)
                .toList();
    }

    public void deleteCategory(Long catId) {
        categoryRepository.deleteById(catId);
    }

    private Category getCategoryOrThrow(Long catId) {
        return categoryRepository.findById(catId).orElseThrow(() -> new NotFoundException("Category not found"));
    }
}
