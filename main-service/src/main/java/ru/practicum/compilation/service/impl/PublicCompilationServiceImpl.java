package ru.practicum.compilation.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.compilation.service.PublicCompilationService;
import ru.practicum.util.ObjectCheckExistence;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.compilation.mapper.CompilationMapper.COMPILATION_MAPPER;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicCompilationServiceImpl implements PublicCompilationService {

    private final CompilationRepository compilationRepository;
    private final ObjectCheckExistence checkExistence;

    @Override
    public List<CompilationDto> findCompilationByPublic(Boolean pinned, PageRequest pageable) {
        Page<Compilation> compilations;

        if (pinned != null) {
            compilations = compilationRepository.findAllByPinned(pinned, pageable);
        } else {
            compilations = compilationRepository.findAll(pageable);
        }
        log.info("Запрошена подборка событий c pinned: {}", pinned);
        return compilations.stream()
                .map(COMPILATION_MAPPER::toCompilationDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto findCompilationById(Long compId) {
        log.info("Запрошена подборка событий с id {}", compId);
        return COMPILATION_MAPPER.toCompilationDto(checkExistence.checkCompilation(compId));
    }
}
