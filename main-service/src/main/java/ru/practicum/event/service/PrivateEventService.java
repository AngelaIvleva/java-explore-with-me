package ru.practicum.event.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;

import java.util.List;

public interface PrivateEventService {

    List<EventFullDto> findAllEventsByUser(Long userId, Pageable pageable);

    EventFullDto addEventByUser(Long userId, NewEventDto newEventDto);

    EventFullDto findEventByUser(Long userId, Long eventId);

    EventFullDto updateEventByUser(Long userId,
                                   Long eventId,
                                   UpdateEventUserRequest userRequest);
}
