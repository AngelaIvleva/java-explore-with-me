package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.util.ObjectCheckExistence;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.category.mapper.CategoryMapper.CATEGORY_MAPPER;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements ru.practicum.category.service.CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final ObjectCheckExistence checkExistence;

    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        Optional<Category> cat = categoryRepository.findByName(newCategoryDto.getName());
        if (cat.isPresent()) {
            log.info("Категория с наименованием {} уже существует", newCategoryDto.getName());
            throw new ConflictException(String.format("Категория с наименованием %s уже существует",
                    newCategoryDto.getName()));
        }
        Category category = categoryRepository.save(CATEGORY_MAPPER.toCategory(newCategoryDto));
        log.info("Добавление категории {} с id {}", category.getName(), category.getId());
        return CATEGORY_MAPPER.toCategoryDto(category);
    }

    @Override
    public CategoryDto updateCategory(Long catId, NewCategoryDto newCategoryDto) {
        Category category = checkExistence.checkCategory(catId);
        Optional<Category> cat = categoryRepository.findByName(newCategoryDto.getName());
        if (cat.isPresent() && !category.getName().equals(newCategoryDto.getName())) {
            log.info("Категория с наименованием {} уже существует", category.getName());
            throw new ConflictException(String.format("Категория с наименованием %s уже существует",
                    newCategoryDto.getName()));
        }
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

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        PageRequest pageable = PageRequest.of(from / size, size);
        log.info("Запрошен список категорий по параметрам");
        return categoryRepository.findAll(pageable).stream()
                .map(CATEGORY_MAPPER::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoryById(Long catId) {
        log.info("Запрошена категория с id {}", catId);
        return CATEGORY_MAPPER.toCategoryDto(checkExistence.checkCategory(catId));
    }
}
