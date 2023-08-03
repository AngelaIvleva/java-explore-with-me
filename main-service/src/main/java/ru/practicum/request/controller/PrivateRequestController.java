package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.PrivateRequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/requests")
public class PrivateRequestController {

    private final PrivateRequestService requestService;

    @GetMapping
    public List<ParticipationRequestDto> get(@PathVariable("userId") Long userId) {
        return requestService.findByRequestorId(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto create(@PathVariable("userId") Long userId,
                                          @RequestParam Long eventId) {
        return requestService.addRequest(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto update(@PathVariable("userId") Long userId,
                                          @PathVariable("requestId") Long requestId) {
        return requestService.cancelRequest(userId, requestId);
    }
}
