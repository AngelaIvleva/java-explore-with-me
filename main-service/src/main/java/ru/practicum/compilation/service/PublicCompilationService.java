package ru.practicum.compilation.service;

import org.springframework.data.domain.PageRequest;
import ru.practicum.compilation.dto.CompilationDto;

import java.util.List;

public interface PublicCompilationService {

    List<CompilationDto> findCompilationByPublic(Boolean pinned, PageRequest pageable);

    CompilationDto findCompilationById(Long compId);

}
