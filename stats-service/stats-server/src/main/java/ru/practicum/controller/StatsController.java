package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;
import ru.practicum.service.StatsService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;
    private static final String FORMATTER = "yyyy-MM-dd HH:mm:ss";

    @PostMapping("/hit")
    public void postHit(@Valid @RequestBody EndpointHit hit) {
        statsService.postHit(hit);
    }

    @GetMapping("/stats")
    public List<ViewStats> getStats(@RequestParam @DateTimeFormat(pattern = FORMATTER) LocalDateTime start,
                                    @RequestParam @DateTimeFormat(pattern = FORMATTER) LocalDateTime end,
                                    @RequestParam(required = false) List<String> uris,
                                    @RequestParam(required = false, defaultValue = "false") Boolean unique) {
        if (start == null || end == null || start.isAfter(end)) {
            throw new IllegalArgumentException(
                    String.format("Недопустимый временной интервал: start %s; end %s", start, end));
        }
        return statsService.getStats(start, end, uris, unique);
    }
}
