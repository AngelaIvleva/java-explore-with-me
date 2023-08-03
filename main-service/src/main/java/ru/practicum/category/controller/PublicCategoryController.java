package ru.practicum.category.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.service.PublicCategoryService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class PublicCategoryController {

    private final PublicCategoryService categoryService;

    @GetMapping
    public List<CategoryDto> get(@PositiveOrZero @RequestParam(value = "from", defaultValue = "0", required = false) int from,
                                 @Positive @RequestParam(value = "size", defaultValue = "10", required = false) int size) {
        return categoryService.getCategories(PageRequest.of(from / size, size));
    }

    @GetMapping("/{catId}")
    public CategoryDto get(@PathVariable("catId") Long catId) {
        return categoryService.getCategoryById(catId);
    }
}
