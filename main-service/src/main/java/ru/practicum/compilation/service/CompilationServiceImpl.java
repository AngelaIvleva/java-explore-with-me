package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.util.ObjectCheckExistence;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.compilation.mapper.CompilationMapper.COMPILATION_MAPPER;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final ObjectCheckExistence checkExistence;

    @Override
    public CompilationDto addCompilationByAdmin(NewCompilationDto newCompilationDto) {
        List<Event> events = new ArrayList<>();
        if (newCompilationDto.getEvents() != null) {
            events = eventRepository.findAllByIdIn(newCompilationDto.getEvents());
        }
        Compilation compilation = new Compilation();
        compilation.setPinned(newCompilationDto.getPinned());
        compilation.setTitle(newCompilationDto.getTitle());
        compilation.setEvents(events);
        compilationRepository.save(compilation);
        log.info("Сохранена подборка событий: title {}", compilation.getTitle());
        return COMPILATION_MAPPER.toCompilationDto(compilation);
    }

    @Override
    public void deleteCompilationByAdmin(Long compId) {
        checkExistence.checkCompilation(compId);
        log.info("Удаление подборки событий с id {}", compId);
        compilationRepository.deleteById(compId);
    }

    @Override
    public CompilationDto updateCompilationByAdmin(Long compId, UpdateCompilationRequest request) {
        Compilation compilation = checkExistence.checkCompilation(compId);
        if (request.getEvents() != null) {
            compilation.setEvents(eventRepository.findAllByIdIn(request.getEvents()));
        }
        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }
        if (request.getTitle() != null) {
            compilation.setTitle(request.getTitle());
        }
        compilationRepository.save(compilation);
        log.info("Обновлена подборка событий с id {}", compId);
        return COMPILATION_MAPPER.toCompilationDto(compilation);
    }

    @Override
    public List<CompilationDto> findCompilationByPublic(Boolean pinned, PageRequest pageable) {
        if (pinned != null) {
            return compilationRepository.findAllByPinned(pinned, pageable)
                    .stream()
                    .map(COMPILATION_MAPPER::toCompilationDto)
                    .collect(Collectors.toList());
        }
        return compilationRepository.findAll(pageable)
                .stream()
                .map(COMPILATION_MAPPER::toCompilationDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto findCompilationById(Long compId) {
        log.info("Запрошена подборка событий с id {}", compId);
        return COMPILATION_MAPPER.toCompilationDto(checkExistence.checkCompilation(compId));
    }
}
