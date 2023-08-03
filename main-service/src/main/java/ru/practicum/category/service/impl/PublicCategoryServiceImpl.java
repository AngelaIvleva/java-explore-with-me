package ru.practicum.category.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.category.service.PublicCategoryService;
import ru.practicum.util.ObjectCheckExistence;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.category.mapper.CategoryMapper.CATEGORY_MAPPER;

@Service
@Slf4j
@RequiredArgsConstructor
public class PublicCategoryServiceImpl implements PublicCategoryService {

    private final CategoryRepository categoryRepository;
    private final ObjectCheckExistence checkExistence;

    @Override
    public List<CategoryDto> getCategories(Pageable pageable) {
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
