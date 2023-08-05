package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;
import ru.practicum.exception.ValidationException;
import ru.practicum.repository.StatsRepository;
import ru.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.mapper.StatsMapper.STATS_MAPPER;

@Slf4j
@RequiredArgsConstructor
@Service
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    @Override
    public void postHit(EndpointHit hit) {
        statsRepository.save(STATS_MAPPER.endpointHitToStats(hit));
        log.info("На uri {} был отправлен запрос", hit.getUri());
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start,
                                    LocalDateTime end,
                                    List<String> uris,
                                    Boolean unique) {
        checkDateTime(start, end);
        List<ViewStats> viewStatsList;

        if (uris == null || uris.isEmpty()) {
            viewStatsList = unique
                    ? statsRepository.getAllUniqueStats(start, end)
                    : statsRepository.getAllStats(start, end);
        } else {
            viewStatsList = unique
                    ? statsRepository.getUniqueStatsByUris(start, end, uris)
                    : statsRepository.getStatsByUris(start, end, uris);
        }

        log.info("Получение статистики по параметрам: uris - {}; startTime - {}; endTime - {}; unique - {}",
                uris, start, end, unique);

        return viewStatsList;
    }

    private void checkDateTime(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new ValidationException(
                    String.format("Недопустимый временной интервал: start %s; end %s", start, end));
        }
    }
}
