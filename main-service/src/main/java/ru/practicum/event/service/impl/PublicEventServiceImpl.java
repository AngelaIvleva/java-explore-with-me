package ru.practicum.event.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.ViewStats;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.enums.Sort;
import ru.practicum.event.enums.State;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.service.PublicEventService;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.enums.RequestStatus;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.util.ObjectCheckExistence;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.event.enums.Sort.VIEWS;
import static ru.practicum.event.mapper.EventMapper.EVENT_MAPPER;

@Service
@Slf4j
@RequiredArgsConstructor
public class PublicEventServiceImpl implements PublicEventService {

    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final StatsClient statsClient;
    private final ObjectCheckExistence checkExistence;
    private static final long DEFAULT_VIEWS = 0L;

    @Override
    @Transactional
    public List<EventShortDto> findAllEventsByPublic(String text,
                                                     List<Long> categories,
                                                     Boolean paid,
                                                     LocalDateTime rangeStart,
                                                     LocalDateTime rangeEnd,
                                                     Boolean onlyAvailable,
                                                     Sort sort,
                                                     Pageable pageable,
                                                     HttpServletRequest request) {

        LocalDateTime defaultRangeStart = LocalDateTime.now();
        LocalDateTime defaultRangeEnd = defaultRangeStart.plusYears(5);
        rangeStart = rangeStart == null ? defaultRangeStart : rangeStart;
        rangeEnd = rangeEnd == null ? defaultRangeEnd : rangeEnd;

        checkExistence.checkDateTime(rangeStart, rangeEnd);

        text = text == null ? "" : text.toLowerCase();
        List<Event> events = eventRepository.findByParamsOrderByDate(
                text,
                List.of(State.PUBLISHED),
                categories,
                paid,
                rangeStart,
                rangeEnd,
                pageable);

        statsClient.postHit("ewm-main-service", request.getRequestURI(), request.getRemoteAddr(), LocalDateTime.now());
        List<Event> eventList = setViewsAndConfirmedRequests(events);

        if (sort != null && sort.equals(VIEWS)) {
            eventList.sort(Comparator.comparing(Event::getViews).reversed());
        }
        log.info("Запрошен список событий /ByPublic");
        return events.stream()
                .filter(event -> !onlyAvailable || event.getParticipantLimit() <= event.getConfirmedRequests())
                .map(EVENT_MAPPER::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public EventFullDto findEventById(Long id, HttpServletRequest request) {
        Event event = checkExistence.checkEvent(id);

        if (event.getState() != State.PUBLISHED) {
            throw new NotFoundException(String.format("Событие %d не опубликовано", event.getId()));
        }

        statsClient.postHit("ewm-main-service", request.getRequestURI(), request.getRemoteAddr(), LocalDateTime.now());

        setEventViews(event, request.getRequestURI());
        setEventConfirmedRequests(event, id);

        log.info("Запрошено событие с id {}", id);
        return EVENT_MAPPER.toEventFullDto(eventRepository.save(event));
    }

    private void setEventViews(Event event, String requestUri) {
        List<ViewStats> viewStatsList = statsClient.getStats(List.of(event.getId()), true);

        long hits = viewStatsList.stream()
                .filter(stats -> Objects.equals(stats.getUri(), requestUri))
                .count();

        event.setViews(hits);
    }

    private void setEventConfirmedRequests(Event event, Long eventId) {
        List<ParticipationRequest> confirmedRequests = requestRepository.findAllByEventIdInAndStatus(List.of(eventId),
                RequestStatus.CONFIRMED);
        event.setConfirmedRequests((long) confirmedRequests.size());
    }

    private List<Event> setViewsAndConfirmedRequests(List<Event> events) {
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        Map<Long, Long> views = getViewStatsMap(eventIds);

        events.forEach(event -> event.setViews(views.getOrDefault(event.getId(), DEFAULT_VIEWS)));

        Map<Event, Long> confirmedRequestsCountMap = getConfirmedRequestsCountMap(eventIds);

        events.forEach(event -> {
            Long confirmedRequestsCount = confirmedRequestsCountMap.getOrDefault(event, DEFAULT_VIEWS);
            event.setConfirmedRequests(confirmedRequestsCount);
        });

        return eventRepository.saveAll(events);
    }

    private Map<Long, Long> getViewStatsMap(List<Long> eventIds) {
        List<ViewStats> viewStatsList = statsClient.getStats(eventIds, false);

        return Optional.ofNullable(viewStatsList)
                .orElse(Collections.emptyList())
                .stream()
                .collect(Collectors.toMap(
                        viewStats -> Long.parseLong(viewStats.getUri()
                                .substring(viewStats.getUri().lastIndexOf("/") + 1)),
                        ViewStats::getHits
                ));
    }

    private Map<Event, Long> getConfirmedRequestsCountMap(List<Long> eventIds) {
        return requestRepository.findAllByEventIdInAndStatus(eventIds, RequestStatus.CONFIRMED)
                .stream()
                .collect(Collectors.groupingBy(ParticipationRequest::getEvent, Collectors.counting()));
    }
}