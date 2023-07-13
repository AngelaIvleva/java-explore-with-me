package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.mapper.StatsMapper.STATS_MAPPER;

@Slf4j
@RequiredArgsConstructor
@Service
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    @Transactional
    @Override
    public void postHit(EndpointHit hit) {
        statsRepository.save(STATS_MAPPER.endpointHitToStats(hit));
        log.info("На uri {} был отправлен запрос", hit.getUri());
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (uris == null || uris.isEmpty()) {
            if (unique) {
                log.info("Получение статистики с {} по {} unique == true", start, end);
                return statsRepository.getAllUniqueStats(start, end);
            } else {
                log.info("Получение статистики с {} по {} unique == false", start, end);
                return statsRepository.getAllStats(start, end);
            }
        } else {
            if (unique) {
                log.info("Получение статистики по uris с {} по {} unique == true", start, end);
                return statsRepository.getUniqueStatsByUris(start, end, uris);
            } else {
                log.info("Получение статистики по uris с {} по {} unique == false", start, end);
                return statsRepository.getStatsByUris(start, end, uris);
            }
        }
    }
}
