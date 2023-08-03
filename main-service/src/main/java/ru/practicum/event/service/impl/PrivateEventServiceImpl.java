package ru.practicum.event.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.category.model.Category;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.enums.State;
import ru.practicum.event.enums.StateAction;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.service.PrivateEventService;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.user.model.User;
import ru.practicum.util.ObjectCheckExistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.event.mapper.EventMapper.EVENT_MAPPER;

@Service
@Slf4j
@RequiredArgsConstructor
public class PrivateEventServiceImpl implements PrivateEventService {

    private final EventRepository eventRepository;
    private final ObjectCheckExistence checkExistence;
    private final EventServiceHelper eventServiceHelper;

    @Override
    public List<EventFullDto> findAllEventsByUser(Long userId, Pageable pageable) {
        checkExistence.checkUser(userId);
        return eventRepository.findAllByInitiatorId(userId, pageable)
                .stream()
                .map(EVENT_MAPPER::toEventFullDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto addEventByUser(Long userId, NewEventDto newEventDto) {
        User user = checkExistence.checkUser(userId);
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Дата и время, на которые намечено событие, " +
                    "не может быть раньше, чем через два часа от текущего момента");
        }
        Category category = checkExistence.checkCategory(newEventDto.getCategory());
        Event event = createEventFromDto(newEventDto, user, category);
        return EVENT_MAPPER.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto findEventByUser(Long userId, Long eventId) {
        Event event = checkExistence.checkEvent(eventId);
        User user = checkExistence.checkUser(userId);

        if (!event.getInitiator().equals(user)) {
            throw new NotFoundException(String.format("Пользователь %s не является создателем события %d",
                    user.getName(), event.getId()));
        }
        return EVENT_MAPPER.toEventFullDto(event);
    }

    @Override
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest request) {
        Event event = EVENT_MAPPER.toEvent(findEventByUser(userId, eventId));
        validateEventDate(request.getEventDate());

        StateAction stateAction = request.getStateAction();
        if (event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("Изменить можно только отмененные события " +
                    "или события в состоянии ожидания модерации");
        } else {
            if (stateAction == StateAction.SEND_TO_REVIEW) {
                event.setState(State.PENDING);
            } else if (stateAction == StateAction.CANCEL_REVIEW) {
                event.setState(State.CANCELED);
            }
        }
        return EVENT_MAPPER.toEventFullDto(eventRepository.save(eventServiceHelper.updateEvent(event, request)));
    }

    private void validateEventDate(LocalDateTime eventDate) {
        if (eventDate != null && eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Дата начала мероприятия не может быть раньше, " +
                    "чем через два часа от текущего момента");
        }
    }

    private Event createEventFromDto(NewEventDto newEventDto, User user, Category category) {
        Event event = EVENT_MAPPER.toEvent(newEventDto);
        event.setCategory(category);
        event.setCreatedOn(LocalDateTime.now());
        event.setInitiator(user);
        event.setState(State.PENDING);
        event.setConfirmedRequests(0L);
        event.setViews(0L);
        return event;
    }
}