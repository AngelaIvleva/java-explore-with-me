package ru.practicum.compilation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.service.CompilationService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/compilations")
public class PublicCompilationController {

    private final CompilationService compilationService;

    @GetMapping
    public List<CompilationDto> get(@RequestParam(value = "pinned", required = false) Boolean pinned,
                                    @PositiveOrZero @RequestParam(value = "from", defaultValue = "0",
                                            required = false) int from,
                                    @Positive @RequestParam(value = "size", defaultValue = "10",
                                            required = false) int size) {
        return compilationService.findCompilationByPublic(pinned, PageRequest.of(from / size, size));
    }

    @GetMapping("/{compId}")
    public CompilationDto getById(@PathVariable("compId") Long compId) {
        return compilationService.findCompilationById(compId);
    }
}
