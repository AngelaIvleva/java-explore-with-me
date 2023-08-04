package ru.practicum.event.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.enums.State;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.service.AdminEventService;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ValidationException;
import ru.practicum.util.ObjectCheckExistence;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.practicum.event.mapper.EventMapper.EVENT_MAPPER;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminEventServiceImpl implements AdminEventService {

    private final EventRepository eventRepository;
    private final ObjectCheckExistence checkExistence;
    private final EventServiceHelper eventServiceHelper;

    @Override
    public List<EventFullDto> findAllEventsByAdmin(List<Long> users,
                                                   List<State> states,
                                                   List<Long> categories,
                                                   LocalDateTime rangeStart,
                                                   LocalDateTime rangeEnd,
                                                   Pageable pageable) {
        if (states == null && rangeStart == null && rangeEnd == null) {
            return eventRepository.findAll(pageable)
                    .stream()
                    .map(EVENT_MAPPER::toEventFullDto)
                    .collect(Collectors.toList());
        }

        checkStates(states);

        if (rangeStart == null) {
            rangeStart = LocalDateTime.now().minusYears(5);
        }

        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(5);
        }
        checkExistence.checkDateTime(rangeStart, rangeEnd);

        List<Event> events = eventRepository.findByParams(
                users,
                states,
                categories,
                rangeStart,
                rangeEnd,
                pageable);

        return events.stream()
                .map(EVENT_MAPPER::toEventFullDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = checkExistence.checkEvent(eventId);
        setEventDate(event, request);
        setEventState(event, request);

        Event eventToSave = eventRepository.save(eventServiceHelper.updateEvent(event, request));
        return EVENT_MAPPER.toEventFullDto(eventToSave);
    }

    private void checkStates(List<State> states) {
        if (states == null) {
            states = new ArrayList<>();
            states.addAll(Stream.of(State.values())
                    .collect(Collectors.toList()));
        }
    }

    private void setEventDate(Event event, UpdateEventAdminRequest request) {
        if (request.getEventDate() != null) {
            if (LocalDateTime.now().isAfter(request.getEventDate())) {
                throw new ValidationException("Дата начала мероприятия должна быть не ранее, " +
                        "чем через час с момента публикации");
            } else event.setEventDate(request.getEventDate());
        }
    }

    private void setEventState(Event event, UpdateEventAdminRequest request) {
        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case PUBLISH_EVENT:
                    setPublishedState(event);
                    break;
                case REJECT_EVENT:
                    setCanceledState(event);
                    break;
                default:
                    throw new ValidationException(String.format("Некорректный StateAction: %s", request.getStateAction()));
            }
        }
    }

    private void setPublishedState(Event event) {
        if (event.getState().equals(State.PENDING)) {
            event.setState(State.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
        } else {
            throw new ConflictException("Событие может быть опубликовано только в том случае, " +
                    "если оно находится в состоянии ожидания публикации");
        }
    }

    private void setCanceledState(Event event) {
        if (event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("Событие может быть отклонено только в том случае, " +
                    "если оно еще не было опубликовано");
        }
        event.setState(State.CANCELED);
    }
}