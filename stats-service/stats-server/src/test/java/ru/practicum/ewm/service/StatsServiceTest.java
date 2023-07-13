package ru.practicum.ewm.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;
import ru.practicum.repository.StatsRepository;
import ru.practicum.service.StatsServiceImpl;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class StatsServiceTest {

    @Mock
    StatsRepository repository;

    @InjectMocks
    StatsServiceImpl service;

    EndpointHit hit = EndpointHit.builder()
            .app("ewm-main-service")
            .ip("192.163.0.1")
            .uri("/events/1")
            .timestamp(LocalDateTime.now())
            .build();

    ViewStats viewStats = ViewStats.builder()
            .app("ewm-main-service")
            .uri("/events/1")
            .hits(6L)
            .build();

    @Test
    void shouldCreateHit() {
        service.postHit(hit);
        verify(repository, Mockito.times(1))
                .save(any());
    }

}
