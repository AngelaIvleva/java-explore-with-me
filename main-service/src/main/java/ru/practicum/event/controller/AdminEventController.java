package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.enums.State;
import ru.practicum.event.service.AdminEventService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/events")
public class AdminEventController {

    private final AdminEventService eventService;

    private static final String FORMATTER = "yyyy-MM-dd HH:mm:ss";

    @GetMapping
    public List<EventFullDto> get(@RequestParam(value = "users", required = false) List<Long> users,
                                  @RequestParam(value = "states", required = false) List<State> states,
                                  @RequestParam(value = "categoriesId", required = false) List<Long> categoriesId,
                                  @RequestParam(value = "rangeStart", required = false)
                                  @DateTimeFormat(pattern = FORMATTER) LocalDateTime rangeStart,
                                  @RequestParam(value = "rangeEnd", required = false)
                                  @DateTimeFormat(pattern = FORMATTER) LocalDateTime rangeEnd,
                                  @PositiveOrZero @RequestParam(value = "from", defaultValue = "0",
                                          required = false) int from,
                                  @Positive @RequestParam(value = "size", defaultValue = "10",
                                          required = false) int size) {
        return eventService.findAllEventsByAdmin(users, states, categoriesId, rangeStart, rangeEnd,
                PageRequest.of(from / size, size));
    }

    @PatchMapping("/{eventId}")
    public EventFullDto update(@PathVariable("eventId") Long eventId,
                               @RequestBody UpdateEventAdminRequest request) {
        return eventService.updateEventByAdmin(eventId, request);
    }
}
