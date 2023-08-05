package ru.practicum.compilation.service;

import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;

public interface AdminCompilationService {
    CompilationDto addCompilationByAdmin(NewCompilationDto newCompilationDto);

    void deleteCompilationByAdmin(Long compId);

    CompilationDto updateCompilationByAdmin(Long compId, UpdateCompilationRequest request);

}
