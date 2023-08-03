package ru.practicum.event.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.enums.State;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminEventService {

    List<EventFullDto> findAllEventsByAdmin(List<Long> userIds,
                                            List<State> states,
                                            List<Long> categoryIds,
                                            LocalDateTime start,
                                            LocalDateTime end,
                                            Pageable pageable);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request);
}