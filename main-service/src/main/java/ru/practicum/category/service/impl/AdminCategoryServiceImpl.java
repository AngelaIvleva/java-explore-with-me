package ru.practicum.category.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.category.service.AdminCategoryService;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.util.ObjectCheckExistence;

import java.util.Optional;

import static ru.practicum.category.mapper.CategoryMapper.CATEGORY_MAPPER;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminCategoryServiceImpl implements AdminCategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final ObjectCheckExistence checkExistence;

    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        checkCategoryNameNotExist(newCategoryDto.getName());
        Category category = categoryRepository.save(CATEGORY_MAPPER.toCategory(newCategoryDto));
        log.info("Добавление категории {} с id {}", category.getName(), category.getId());
        return CATEGORY_MAPPER.toCategoryDto(category);
    }

    @Override
    public CategoryDto updateCategory(Long catId, NewCategoryDto newCategoryDto) {
        Category category = checkExistence.checkCategory(catId);
        checkCategoryNameNotConflict(category, newCategoryDto.getName());

        category.setName(newCategoryDto.getName());
        log.info("Обновление категории с id {}", category.getId());
        return CATEGORY_MAPPER.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(Long catId) {
        if (!eventRepository.findAllByCategoryId(catId).isEmpty()) {
            throw new ConflictException("Нельзя удалить категорию, если существуют связянные с ней события");
        }
        Category category = checkExistence.checkCategory(catId);
        log.info("Удаление категории {} с id {}", category.getName(), category.getId());
        categoryRepository.deleteById(catId);
    }

    private void checkCategoryNameNotExist(String categoryName) {
        Optional<Category> existingCategory = categoryRepository.findByName(categoryName);
        if (existingCategory.isPresent()) {
            log.info("Категория с наименованием {} уже существует", categoryName);
            throw new ConflictException(String.format("Категория с наименованием %s уже существует", categoryName));
        }
    }

    private void checkCategoryNameNotConflict(Category category, String newCategoryName) {
        Optional<Category> existingCategory = categoryRepository.findByName(newCategoryName);
        if (existingCategory.isPresent() && !category.getName().equals(newCategoryName)) {
            log.info("Категория с наименованием {} уже существует", category.getName());
            throw new ConflictException(String.format("Категория с наименованием %s уже существует", newCategoryName));
        }
    }
}
