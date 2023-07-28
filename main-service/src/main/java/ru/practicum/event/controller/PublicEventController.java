package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.enums.Sort;
import ru.practicum.event.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class PublicEventController {

    private final EventService eventService;
    private static final String FORMATTER = "yyyy-MM-dd HH:mm:ss";

    @GetMapping
    public List<EventShortDto> getAllEvents(@RequestParam(value = "text", required = false) String text,
                                            @RequestParam(value = "categories", required = false) List<Long> categories,
                                            @RequestParam(value = "paid", required = false) Boolean paid,
                                            @RequestParam(value = "rangeStart", required = false)
                                            @DateTimeFormat(pattern = FORMATTER) LocalDateTime rangeStart,
                                            @RequestParam(value = "rangeEnd", required = false)
                                            @DateTimeFormat(pattern = FORMATTER) LocalDateTime rangeEnd,
                                            @RequestParam(value = "onlyAvailable", defaultValue = "false",
                                                    required = false) Boolean onlyAvailable,
                                            @RequestParam(value = "sort", required = false) Sort sort,
                                            @PositiveOrZero @RequestParam(value = "from", defaultValue = "0",
                                                    required = false) int from,
                                            @Positive @RequestParam(value = "size", defaultValue = "10",
                                                    required = false) int size,
                                            HttpServletRequest request) {
        return eventService.findAllEventsByPublic(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort,
                PageRequest.of(from / size, size), request);
    }

    @GetMapping("/{id}")
    public EventFullDto getEvent(@PathVariable Long id, HttpServletRequest request) {
        return eventService.findEventById(id, request);
    }
}
