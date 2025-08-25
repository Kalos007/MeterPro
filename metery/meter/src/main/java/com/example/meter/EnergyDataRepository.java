package com.example.meter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

// EnergyDataRepository.java
public interface EnergyDataRepository extends JpaRepository<EnergyData, Long> {
    List<EnergyData> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    
//    @Query(value = "SELECT DATE_TRUNC('hour', timestamp - INTERVAL '4 hours') + INTERVAL '4 hours' AS timestamp, " +
//            "ROUND(AVG(watts)) AS watts " +
//            "FROM energy_data " +
//            "WHERE timestamp BETWEEN :start AND :end " +
//            "GROUP BY DATE_TRUNC('hour', timestamp - INTERVAL '4 hours') " +
//            "ORDER BY timestamp", nativeQuery = true)
//    List<Object[]> findByTimestampBetweenWithFourHourAggregation(LocalDateTime start, LocalDateTime end);

//    @Query("""
//        SELECT
//            FUNCTION('DATE', e.timestamp) AS date,
//            AVG(e.watts) AS avgWatts,
//            MAX(e.watts) AS peakWatts
//        FROM EnergyData e
//        WHERE e.meter.meterNumber = :meterNumber
//          AND (:start IS NULL OR e.timestamp >= :start)
//          AND (:end IS NULL OR e.timestamp <= :end)
//        GROUP BY FUNCTION('DATE', e.timestamp)
//        ORDER BY FUNCTION('DATE', e.timestamp) DESC
//        """)
//    List<DailyUsageSummary> findDailyUsageSummaries(
//            @Param("meterNumber") String meterNumber,
//            @Param("start") LocalDateTime start,
//            @Param("end") LocalDateTime end
//    );


        @Query("SELECT AVG(p.watts) FROM EnergyData p WHERE p.timestamp BETWEEN :start AND :end")
        Double findAverageWattsBetween(@Param("start") LocalDateTime start,
                @Param("end") LocalDateTime end);

        @Query("SELECT MAX(p.watts) FROM EnergyData p WHERE p.timestamp BETWEEN :start AND :end")
        Double findMaxWattsBetween(@Param("start") LocalDateTime start,
                @Param("end") LocalDateTime end);



}