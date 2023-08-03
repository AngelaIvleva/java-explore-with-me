package ru.practicum.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ValidationException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.enums.RequestStatus;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.request.service.PrivateRequestService;
import ru.practicum.user.model.User;
import ru.practicum.util.ObjectCheckExistence;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.event.enums.State.PUBLISHED;
import static ru.practicum.request.enums.RequestStatus.*;
import static ru.practicum.request.mapper.RequestMapper.REQUEST_MAPPER;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrivateRequestServiceImpl implements PrivateRequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final ObjectCheckExistence checkExistence;

  /*  @Override
    public List<ParticipationRequestDto> findByRequestorId(Long userId) {
        User user = checkExistence.checkUser(userId);
        log.info("Получение информации о заявках текущего пользователя id {} на участие в чужих событиях", userId);
        return requestRepository.findAllByRequester(user).stream()
                .map(REQUEST_MAPPER::toParticipationRequestDto)
                .collect(Collectors.toList());
    }


    @Override
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        User user = checkExistence.checkUser(userId);
        Event event = checkExistence.checkEvent(eventId);

        if (event.getInitiator().equals(user)) {
            throw new ConflictException("Инициатор события не может добавить запрос на участие в своём событии");
        }
        if (event.getState() != PUBLISHED) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии");
        }

        Optional<ParticipationRequest> req = requestRepository.findByRequesterIdAndEventId(userId, eventId);
        if (req.isPresent()) {
            throw new ConflictException("Нельзя добавить повторный запрос");
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(user)
                .status(event.getRequestModeration() ? PENDING : CONFIRMED)
                .build();

        if (event.getParticipantLimit() > 0 && event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ConflictException("Достигнут лимит запросов на участие");
        } else {
            request.setStatus(RequestStatus.CONFIRMED);
        }

        if (request.getStatus() == RequestStatus.CONFIRMED) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }
        log.info("Добавление запроса на участие в событии id {}", event.getId());
        return REQUEST_MAPPER.toParticipationRequestDto(requestRepository.save(request));
    }


    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        User user = checkExistence.checkUser(userId);
        ParticipationRequest request = checkExistence.checkRequest(requestId);
        if (!request.getRequester().equals(user)) {
            throw new ConflictException("Нельзя отменить чужой запрос на участие");
        }
        request.setStatus(RequestStatus.CANCELED);
        log.info("Отмена запроса id {} на участие в событии пользователем id {}", requestId, userId);
        return REQUEST_MAPPER.toParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getRequestsEventByUser(Long userId, Long eventId) {
        User user = checkExistence.checkUser(userId);
        Event event = checkExistence.checkEvent(eventId);

        if (!event.getInitiator().equals(user)) {
            throw new ValidationException("Принять заявку может только инициатор события");
        }
        log.info("Получение информации о запросах на участие в событии id {} пользователя id {}", eventId, userId);
        return requestRepository.findAllByEvent(event)
                .stream()
                .map(REQUEST_MAPPER::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestsByUser(Long userId,
                                                               Long eventId,
                                                               EventRequestStatusUpdateRequest updateRequest) {

        User user = checkExistence.checkUser(userId);
        Event event = checkExistence.checkEvent(eventId);

        if (!event.getInitiator().equals(user)) {
            throw new ValidationException("Принять заявку может только инициатор события");
        }
        if (event.getParticipantLimit() <= event.getConfirmedRequests() && event.getParticipantLimit() != 0) {
            throw new ConflictException("нельзя подтвердить заявку, если уже достигнут лимит заявок на данное событие");
        }

        List<ParticipationRequest> requestList = requestRepository.findAllByIdIn(updateRequest.getRequestIds());

        if (requestList.size() != updateRequest.getRequestIds().size()) {
            throw new ConflictException("События не найдены");
        }
        log.info("Изменение статуса (подтверждена, отменена) заявок на участие в событии id {} пользователя id {}",
                eventId, userId);

        if (updateRequest.getStatus().equals(CONFIRMED)) {
            return updateConfirmedStatus(requestList, event);
        } else if (updateRequest.getStatus().equals(REJECTED)) {
            return updateRejectedStatus(requestList);
        } else {
            throw new ValidationException("Некорректный статус");
        }
    }

    private EventRequestStatusUpdateResult updateConfirmedStatus(List<ParticipationRequest> requestList, Event event) {
        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        for (ParticipationRequest request : requestList) {
            validateRequestStatus(request);

            if (event.getParticipantLimit() <= event.getConfirmedRequests()) {
                request.setStatus(REJECTED);
                rejected.add(REQUEST_MAPPER.toParticipationRequestDto(request));
            } else {
                request.setStatus(CONFIRMED);
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                confirmed.add(REQUEST_MAPPER.toParticipationRequestDto(request));
            }
        }
        requestRepository.saveAll(requestList);
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmed)
                .rejectedRequests(rejected)
                .build();
    }

    private EventRequestStatusUpdateResult updateRejectedStatus(List<ParticipationRequest> requestList) {
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        for (ParticipationRequest request : requestList) {
            validateRequestStatus(request);

            request.setStatus(REJECTED);
            requestRepository.save(request);
            rejected.add(REQUEST_MAPPER.toParticipationRequestDto(request));
        }
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(new ArrayList<>())
                .rejectedRequests(rejected)
                .build();
    }

    private void validateRequestStatus(ParticipationRequest request) {
        if (!request.getStatus().equals(RequestStatus.PENDING)) {
            throw new ConflictException("Статус заявки должен быть PENDING");
        }
    }
}*/

    @Override
    public List<ParticipationRequestDto> findByRequestorId(Long userId) {
        User user = checkExistence.checkUser(userId);
        log.info("Получение информации о заявках текущего пользователя id {} на участие в чужих событиях", userId);
        return requestRepository.findAllByRequester(user).stream()
                .map(REQUEST_MAPPER::toParticipationRequestDto)
                .collect(Collectors.toList());
    }


    @Override
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        User user = checkExistence.checkUser(userId);
        Event event = checkExistence.checkEvent(eventId);

        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(user)
                .status(event.getRequestModeration() ? PENDING : CONFIRMED)
                .build();

        Optional<ParticipationRequest> req = requestRepository.findByRequesterIdAndEventId(userId, eventId);
        if (req.isPresent()) {
            throw new ConflictException("Нельзя добавить повторный запрос");
        }
        if (event.getInitiator().equals(user)) {
            throw new ConflictException("Инициатор события не может добавить запрос на участие в своём событии");
        }
        if (event.getState() != PUBLISHED) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии");
        }
        if (event.getParticipantLimit() > 0) {
            if (event.getConfirmedRequests() >= event.getParticipantLimit()) {
                throw new ConflictException("Достигнут лимит запросов на участие");
            }
        } else {
            request.setStatus(RequestStatus.CONFIRMED);
        }

        if (request.getStatus() == CONFIRMED) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }
        log.info("Добавление запроса на участие в событии id {}", event.getId());
        return REQUEST_MAPPER.toParticipationRequestDto(requestRepository.save(request));
    }


    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        User user = checkExistence.checkUser(userId);
        ParticipationRequest request = checkExistence.checkRequest(requestId);

        if (!request.getRequester().equals(user)) {
            throw new ConflictException("Нельзя отменить чужой запрос на участие");
        }
        request.setStatus(RequestStatus.CANCELED);
        log.info("Отмена запроса id {} на участие в событии пользователем id {}", requestId, userId);
        return REQUEST_MAPPER.toParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getRequestsEventByUser(Long userId, Long eventId) {
        User user = checkExistence.checkUser(userId);
        Event event = checkExistence.checkEvent(eventId);

        if (!event.getInitiator().equals(user)) {
            throw new ValidationException("Принять заявку может только инициатор события");
        }
        log.info("Получение информации о запросах на участие в событии id {} пользователя id {}", eventId, userId);
        return requestRepository.findAllByEvent(event)
                .stream()
                .map(REQUEST_MAPPER::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestsByUser(Long userId,
                                                               Long eventId,
                                                               EventRequestStatusUpdateRequest updateRequest) {

        User user = checkExistence.checkUser(userId);
        Event event = checkExistence.checkEvent(eventId);

        if (!event.getInitiator().equals(user)) {
            throw new ValidationException("Принять заявку может только инициатор события");
        }
        if (event.getParticipantLimit() <= event.getConfirmedRequests() && event.getParticipantLimit() != 0) {
            throw new ConflictException("нельзя подтвердить заявку, если уже достигнут лимит заявок на данное событие");
        }

        List<ParticipationRequest> requestList = requestRepository.findAllByIdIn(updateRequest.getRequestIds());

        if (requestList.size() != updateRequest.getRequestIds().size()) {
            throw new ConflictException("События не найдены");
        }
        log.info("Изменение статуса (подтверждена, отменена) заявок на участие в событии id {} пользователя id {}",
                eventId, userId);

        if (updateRequest.getStatus().equals(CONFIRMED)) {
            return updateConfirmedStatus(requestList, event);
        } else if (updateRequest.getStatus().equals(REJECTED)) {
            return updateRejectedStatus(requestList);
        } else {
            throw new ValidationException("Некорректный статус");
        }
    }

    private EventRequestStatusUpdateResult updateConfirmedStatus(List<ParticipationRequest> requestList, Event event) {
        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        for (ParticipationRequest request : requestList) {
            if (!request.getStatus().equals(PENDING)) {
                throw new ConflictException("Статус заявки должен быть PENDING");
            }
            if (event.getParticipantLimit() <= event.getConfirmedRequests()) {
                request.setStatus(REJECTED);
                rejected.add(REQUEST_MAPPER.toParticipationRequestDto(request));
            } else {
                request.setStatus(CONFIRMED);
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                confirmed.add(REQUEST_MAPPER.toParticipationRequestDto(request));
            }
        }
        requestRepository.saveAll(requestList);
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmed)
                .rejectedRequests(rejected)
                .build();
    }

    private EventRequestStatusUpdateResult updateRejectedStatus(List<ParticipationRequest> requestList) {
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        for (ParticipationRequest request : requestList) {
            if (!request.getStatus().equals(PENDING)) {
                throw new ConflictException("Статус заявки должен быть PENDING");
            }
            request.setStatus(REJECTED);
            requestRepository.save(request);
            rejected.add(REQUEST_MAPPER.toParticipationRequestDto(request));
        }
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(new ArrayList<>())
                .rejectedRequests(rejected)
                .build();
    }
}
