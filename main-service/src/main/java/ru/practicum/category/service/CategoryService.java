package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        log.info("Администратор создаёт категорию с названием '{}'", newCategoryDto.getName());
        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new AlreadyExistsException("Категория с названием " + newCategoryDto.getName() + " уже существует");
        }

        Category category = CategoryMapper.toCategory(newCategoryDto);
        Category savedCategory = categoryRepository.save(category);
        log.info("Администратор создал категорию {}", savedCategory.getId());
        return CategoryMapper.toCategoryDto(savedCategory);
    }

    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        log.info("Администратор обновляет категорию {} новым названием '{}'", catId, categoryDto.getName());
        Category category = getCategoryOrThrow(catId);

        if (categoryRepository.existsByName(categoryDto.getName()) && !categoryDto.getName().equals(category.getName())) {
            throw new AlreadyExistsException("Категория с названием " + categoryDto.getName() + " уже существует");
        }

        category = CategoryMapper.updateCategoryName(category, categoryDto.getName());
        log.info("Администратор обновил категорию {} новым названием '{}'", catId, categoryDto.getName());
        Category updatedCategory = categoryRepository.save(category);
        return CategoryMapper.toCategoryDto(updatedCategory);
    }

    public CategoryDto getCategory(Long catId) {
        log.info("Пользователь запросил информацию о категории {}", catId);
        return CategoryMapper.toCategoryDto(getCategoryOrThrow(catId));
    }

    public Collection<CategoryDto> getAllCategories(Integer from, Integer size) {
        log.info("Пользователь запросил список категорий: from={}, size={}", from, size);
        Collection<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .sorted(Comparator.comparing(Category::getId))
                .skip(from)
                .limit(size)
                .map(CategoryMapper::toCategoryDto)
                .toList();
    }

    public void deleteCategory(Long catId) {
        log.info("Администратор удаляет категорию {}", catId);
        categoryRepository.deleteById(catId);
    }

    private Category getCategoryOrThrow(Long catId) {
        return categoryRepository.findById(catId).orElseThrow(() -> new NotFoundException("Категория не найдена"));
    }
}
