package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.dto.ViewStats;
import ru.practicum.model.Stats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<Stats, Long> {
    @Query("SELECT new ru.practicum.dto.ViewStats(st.app, st.uri, COUNT(DISTINCT st.ip)) " +
            "FROM Stats AS st " +
            "WHERE st.timestamp BETWEEN :start AND :end " +
            "GROUP BY st.app, st.uri " +
            "ORDER BY COUNT(DISTINCT st.ip) DESC")
    List<ViewStats> getAllUniqueStats(@Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end);

    @Query("SELECT new ru.practicum.dto.ViewStats(st.app, st.uri, COUNT(st.ip)) " +
            "FROM Stats AS st " +
            "WHERE st.timestamp BETWEEN :start AND :end " +
            "GROUP BY st.app, st.uri " +
            "ORDER BY COUNT(st.ip) DESC")
    List<ViewStats> getAllStats(@Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);

    @Query("SELECT new ru.practicum.dto.ViewStats(st.app, st.uri, COUNT(DISTINCT st.ip)) " +
            "FROM Stats AS st " +
            "WHERE st.timestamp BETWEEN :start AND :end " +
            "AND st.uri IN :uris " +
            "GROUP BY st.app, st.uri " +
            "ORDER BY COUNT(DISTINCT st.ip) DESC")
    List<ViewStats> getUniqueStatsByUris(@Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end,
                                         @Param("uris") List<String> uris);

    @Query("SELECT new ru.practicum.dto.ViewStats(st.app, st.uri, COUNT(st.ip)) " +
            "FROM Stats AS st " +
            "WHERE st.timestamp BETWEEN :start AND :end " +
            "AND st.uri IN :uris " +
            "GROUP BY st.app, st.uri " +
            "ORDER BY COUNT(st.ip) DESC")
    List<ViewStats> getStatsByUris(@Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end,
                                   @Param("uris") List<String> uris);
}
