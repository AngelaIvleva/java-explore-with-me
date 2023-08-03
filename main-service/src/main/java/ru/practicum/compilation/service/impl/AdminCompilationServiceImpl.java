package ru.practicum.compilation.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.compilation.service.AdminCompilationService;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.util.ObjectCheckExistence;

import java.util.ArrayList;
import java.util.List;

import static ru.practicum.compilation.mapper.CompilationMapper.COMPILATION_MAPPER;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCompilationServiceImpl implements AdminCompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final ObjectCheckExistence checkExistence;

    @Override
    public CompilationDto addCompilationByAdmin(NewCompilationDto newCompilationDto) {
        List<Event> events = newCompilationDto.getEvents() != null
                ? eventRepository.findAllByIdIn(newCompilationDto.getEvents())
                : new ArrayList<>();

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
}
