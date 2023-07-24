package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.ViewStats;
import ru.practicum.event.dto.*;
import ru.practicum.event.enums.Sort;
import ru.practicum.event.enums.State;
import ru.practicum.event.enums.StateAction;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.location.repository.LocationRepository;
import ru.practicum.request.enums.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.util.ObjectCheckExistence;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.practicum.event.enums.Sort.VIEWS;
import static ru.practicum.event.mapper.EventMapper.EVENT_MAPPER;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final StatsClient statsClient;
    private final ObjectCheckExistence checkExistence;
    private final LocationRepository locationRepository;

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
        if (states == null) {
            states = new ArrayList<>();
            states.addAll(Stream.of(State.values())
                    .collect(Collectors.toList()));
        }

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
        return setViewsAndConfirmedRequests(events);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = checkExistence.checkEvent(eventId);
        if (request.getEventDate() != null) {
            if (LocalDateTime.now().isAfter(request.getEventDate())) {
                throw new ValidationException("Дата начала мероприятия должна быть не ранее, " +
                        "чем через час с момента публикации");
            } else event.setEventDate(request.getEventDate());
        }
        if (request.getStateAction() != null) {
            if (request.getStateAction() == StateAction.PUBLISH_EVENT) {
                if (event.getState().equals(State.PENDING)) {
                    event.setState(State.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                } else {
                    throw new ConflictException("Событие может быть опубликовано только в том случае, " +
                            "если оно находится в состоянии ожидания публикации" +
                            request.getStateAction());
                }
            }
            if (request.getStateAction() == StateAction.REJECT_EVENT) {
                if (event.getState().equals(State.PUBLISHED)) {
                    throw new ConflictException("Событие может быть отклонено только в том случае, " +
                            "если оно еще не было опубликовано" +
                            request.getStateAction());
                }
                event.setState(State.CANCELED);
            }
        }
        return EVENT_MAPPER.toEventFullDto(eventRepository.save(updateEvent(event, request)));
    }

    @Override
    public List<EventShortDto> findAllEventsByPublic(String text,
                                                     List<Long> categories,
                                                     Boolean paid,
                                                     LocalDateTime rangeStart,
                                                     LocalDateTime rangeEnd,
                                                     Boolean onlyAvailable,
                                                     Sort sort,
                                                     Pageable pageable,
                                                     HttpServletRequest request) {
        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(5);
        }
        checkExistence.checkDateTime(rangeStart, rangeEnd);
        if (text == null) text = "";
        List<Event> events = eventRepository.findByParamsOrderByDate(
                text.toLowerCase(),
                List.of(State.PUBLISHED),
                categories,
                paid,
                rangeStart,
                rangeEnd,
                pageable);

        statsClient.postHit("ewm-main-service", request.getRequestURI(), request.getRemoteAddr(), LocalDateTime.now());
        List<EventFullDto> eventFullDtoList = setViewsAndConfirmedRequests(events);

        if (sort != null && sort.equals(VIEWS))
            eventFullDtoList.sort((e1, e2) -> e2.getViews().compareTo(e1.getViews()));
        if (onlyAvailable) {
            return eventFullDtoList.stream()
                    .filter(event -> event.getParticipantLimit() <= event.getConfirmedRequests())
                    .map(EVENT_MAPPER::toEventShortDto)
                    .collect(Collectors.toList());
        } else {
            return eventFullDtoList.stream()
                    .map(EVENT_MAPPER::toEventShortDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public EventFullDto findEventById(Long id, HttpServletRequest request) {
        Event event = checkExistence.checkEvent(id);
        if (event.getState() != State.PUBLISHED) {
            throw new NotFoundException(String.format("Событие %d не опубликовано", event.getId()));
        }
        statsClient.postHit("ewm-main-service", request.getRequestURI(), request.getRemoteAddr(), LocalDateTime.now());
        List<ViewStats> viewStatsList = statsClient.getStats(List.of(id), true);
        long hits = viewStatsList
                .stream()
                .filter(s -> Objects.equals(s.getUri(), request.getRequestURI()))
                .count();
        EventFullDto eventFullDto = EVENT_MAPPER.toEventFullDto(event);
        eventFullDto.setViews(hits + 1);
        eventFullDto.setConfirmedRequests(requestRepository.findAllByEventIdInAndStatus(List.of(id),
                RequestStatus.CONFIRMED).size());
        log.info("Запрошено событие с id {}", id);
        return eventFullDto;
    }

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
        Event event = EVENT_MAPPER.toEvent(newEventDto);
        event.setState(State.PENDING);
        event.setCreatedOn(LocalDateTime.now());
        event.setConfirmedRequests(0L);

        Category category = checkExistence.checkCategory(newEventDto.getCategory());

        event.setCategory(category);
        event.setInitiator(user);
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
    public EventFullDto updateEventByUser(Long userId,
                                          Long eventId,
                                          UpdateEventUserRequest request) {

        Event event = EVENT_MAPPER.toEvent(findEventByUser(userId, eventId));

        if (request.getEventDate() == null || request.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
            if (event.getState().equals(State.PUBLISHED)) {
                throw new ConflictException("Изменить можно только отмененные события " +
                        "или события в состоянии ожидания модерации");
            }
            if (StateAction.SEND_TO_REVIEW == request.getStateAction()) {
                event.setState(State.PENDING);
            }
            if (StateAction.CANCEL_REVIEW == request.getStateAction()) {
                event.setState(State.CANCELED);
            }
        } else {
            throw new ValidationException("Дата начала мероприятия не может быть раньше, " +
                    "чем через два часа от текущего момента");
        }
        return EVENT_MAPPER.toEventFullDto(eventRepository.save(updateEvent(event, request)));
    }

    private Event updateEvent(Event event, UpdateEventRequest request) {
        if (request.getAnnotation() != null) {
            if (request.getAnnotation().length() <= 2000 && request.getAnnotation().length() >= 20) {
                event.setAnnotation(request.getAnnotation());
            } else {
                throw new ValidationException("Аннотация не может быть короче 20 символов и длиннее 2000");
            }
        }
        if (request.getCategory() != null) {
            event.setCategory(checkExistence.checkCategory(request.getCategory()));
        }
        if (request.getDescription() != null) {
            if (request.getDescription().length() <= 7000 && request.getDescription().length() >= 20) {
                event.setDescription(request.getDescription());
            } else {
                throw new ValidationException("Описание не может быть короче 20 символов и длиннее 7000");
            }
        }
        if (request.getLocation() != null) {
            event.setLocation(locationRepository.save(request.getLocation()));
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getTitle() != null) {
            if (request.getTitle().length() >= 3 && request.getTitle().length() <= 120) {
                event.setTitle(request.getTitle());
            } else {
                throw new ValidationException("заголовок не может быть короче 3х символов и длиннее 120");
            }
        }
        return event;
    }

    private List<EventFullDto> setViewsAndConfirmedRequests(List<Event> events) {
        List<Long> eventIds = events
                .stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        List<ViewStats> viewStatsList = statsClient.getStats(eventIds, false);
        Map<Long, Long> views;

        if (viewStatsList != null && !viewStatsList.isEmpty()) {
            views = viewStatsList
                    .stream()
                    .collect(Collectors.toMap(this::getEventIdFromURI, ViewStats::getHits));
        } else {
            views = Collections.emptyMap();
        }
        List<EventFullDto> eventFullDtoList = events
                .stream()
                .map(EVENT_MAPPER::toEventFullDto)
                .collect(Collectors.toList());

        eventFullDtoList.forEach(eventFullDto ->
                eventFullDto.setViews(views.getOrDefault(eventFullDto.getId(), 0L)));

        eventFullDtoList.forEach(eventFullDto ->
                eventFullDto.setConfirmedRequests(requestRepository.findAllByEventIdInAndStatus(new ArrayList<>(eventIds),
                        RequestStatus.CONFIRMED).size()));
        return eventFullDtoList;
    }

    private Long getEventIdFromURI(ViewStats viewStats) {
        return Long.parseLong(viewStats.getUri().substring(viewStats.getUri().lastIndexOf("/") + 1));
    }
}