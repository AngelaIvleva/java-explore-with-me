package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.service.PrivateEventService;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.PrivateRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events")
public class PrivateEventController {

    private final PrivateEventService eventService;

    private final PrivateRequestService requestService;

    @GetMapping
    public List<EventFullDto> getAllEvents(@PathVariable Long userId,
                                           @PositiveOrZero @RequestParam(value = "from", defaultValue = "0",
                                                   required = false) int from,
                                           @Positive @RequestParam(value = "size", defaultValue = "10",
                                                   required = false) int size) {
        return eventService.findAllEventsByUser(userId, PageRequest.of(from / size, size));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto create(@PathVariable Long userId,
                               @Valid @RequestBody NewEventDto newEventDto) {
        return eventService.addEventByUser(userId, newEventDto);
    }

    @GetMapping("/{eventId}")
    public EventFullDto get(@PathVariable("userId") Long userId,
                            @PathVariable("eventId") Long eventId) {
        return eventService.findEventByUser(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto update(@PathVariable("userId") Long userId,
                               @PathVariable("eventId") Long eventId,
                               @Valid @RequestBody UpdateEventUserRequest userRequest) {
        return eventService.updateEventByUser(userId, eventId, userRequest);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsEventByUser(@PathVariable("userId") Long userId,
                                                                @PathVariable("eventId") Long eventId) {
        return requestService.getRequestsEventByUser(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequests(@PathVariable("userId") Long userId,
                                                         @PathVariable("eventId") Long eventId,
                                                         @RequestBody EventRequestStatusUpdateRequest updateRequest) {
        return requestService.updateRequestsByUser(userId, eventId, updateRequest);
    }
}
