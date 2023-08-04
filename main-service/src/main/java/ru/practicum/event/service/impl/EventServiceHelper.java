package ru.practicum.event.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.event.dto.UpdateEventRequest;
import ru.practicum.event.model.Event;
import ru.practicum.exception.ValidationException;
import ru.practicum.location.model.Location;
import ru.practicum.location.repository.LocationRepository;
import ru.practicum.util.ObjectCheckExistence;

@Service
@RequiredArgsConstructor
public class EventServiceHelper {

    private final LocationRepository locationRepository;
    private final ObjectCheckExistence checkExistence;

    private static final int MIN_ANNOTATION_LENGTH = 20;
    private static final int MAX_ANNOTATION_LENGTH = 2000;
    private static final int MIN_DESCRIPTION_LENGTH = 20;
    private static final int MAX_DESCRIPTION_LENGTH = 7000;
    private static final int MIN_TITLE_LENGTH = 3;
    private static final int MAX_TITLE_LENGTH = 120;

    public Event updateEvent(Event event, UpdateEventRequest request) {
        updateAnnotation(event, request.getAnnotation());
        updateCategory(event, request.getCategory());
        updateDescription(event, request.getDescription());
        updateLocation(event, request.getLocation());
        updatePaid(event, request.getPaid());
        updateParticipantLimit(event, request.getParticipantLimit());
        updateRequestModeration(event, request.getRequestModeration());
        updateTitle(event, request.getTitle());

        return event;
    }

    private void updateAnnotation(Event event, String annotation) {
        if (annotation != null) {
            if (annotation.length() < MIN_ANNOTATION_LENGTH || annotation.length() > MAX_ANNOTATION_LENGTH) {
                throw new ValidationException("Аннотация не может быть короче " + MIN_ANNOTATION_LENGTH +
                        " символов и длиннее " + MAX_ANNOTATION_LENGTH);
            }
            event.setAnnotation(annotation);
        }
    }

    private void updateCategory(Event event, Long categoryId) {
        if (categoryId != null) {
            event.setCategory(checkExistence.checkCategory(categoryId));
        }
    }

    private void updateDescription(Event event, String description) {
        if (description != null) {
            if (description.length() < MIN_DESCRIPTION_LENGTH || description.length() > MAX_DESCRIPTION_LENGTH) {
                throw new ValidationException("Описание не может быть короче " + MIN_DESCRIPTION_LENGTH +
                        " символов и длиннее " + MAX_DESCRIPTION_LENGTH);
            }
            event.setDescription(description);
        }
    }

    private void updateLocation(Event event, Location location) {
        if (location != null) {
            event.setLocation(locationRepository.save(location));
        }
    }

    private void updatePaid(Event event, Boolean paid) {
        if (paid != null) {
            event.setPaid(paid);
        }
    }

    private void updateParticipantLimit(Event event, Integer participantLimit) {
        if (participantLimit != null) {
            event.setParticipantLimit(participantLimit);
        }
    }

    private void updateRequestModeration(Event event, Boolean requestModeration) {
        if (requestModeration != null) {
            event.setRequestModeration(requestModeration);
        }
    }

    private void updateTitle(Event event, String title) {
        if (title != null) {
            if (title.length() < MIN_TITLE_LENGTH || title.length() > MAX_TITLE_LENGTH) {
                throw new ValidationException("Заголовок не может быть короче " + MIN_TITLE_LENGTH +
                        " символов и длиннее " + MAX_TITLE_LENGTH);
            }
            event.setTitle(title);
        }
    }
}
