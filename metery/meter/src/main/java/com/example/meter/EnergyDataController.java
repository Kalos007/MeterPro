package com.example.meter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

// EnergyDataController.java
@RestController
@RequestMapping("/api")
public class EnergyDataController {

    @Autowired
    private EnergyDataRepository repository;

    @Autowired
    private MeterRepository meterRepository;

    @Autowired
    private WebConfig webConfig;

    @Autowired
    private PowerService powerService;

    private EnergyData energyData;

    // Save data from ESP32
    @PostMapping("/postUnit")
    public EnergyData saveData(@RequestBody EnergyData data) {
        System.out.println("=== POST RECEIVED ===");
        System.out.println("Units: " + data.getUnits());
        System.out.println("Watts: " + data.getWatts());

        data.setTimestamp(LocalDateTime.now());
        EnergyData saved = repository.save(data);

        System.out.println("Saved ID: " + saved.getId());
        return saved;
    }


    // Fetch data for frontend
    @GetMapping("/meter/latest")
    public List<EnergyData> getData(
            @RequestParam(required = false) String timeRange,
            @RequestParam(required = false) LocalDateTime start,
            @RequestParam(required = false) LocalDateTime end) {

        List<EnergyData> rawData;

        if (start != null && end != null) {
            rawData = repository.findByTimestampBetween(start, end);
        } else {
            rawData = repository.findAll();
        }

        if ("day".equalsIgnoreCase(timeRange)) {
            List<EnergyData> aggregatedData = new ArrayList<>();
            int intervalHours = 4;
            int pointsPerInterval = 24 / intervalHours;

            for (int i = 0; i < pointsPerInterval; i++) {
                LocalDateTime intervalStart = start.plusHours(i * intervalHours);
                LocalDateTime intervalEnd = intervalStart.plusHours(intervalHours);

                List<EnergyData> intervalData = rawData.stream()
                        .filter(data -> !data.getTimestamp().isBefore(intervalStart) && data.getTimestamp().isBefore(intervalEnd))
                        .collect(Collectors.toList());

                if (!intervalData.isEmpty()) {
                    long avgWatts = Math.round(intervalData.stream()
                            .mapToLong(EnergyData::getWatts)
                            .average()
                            .orElse(0.0));

                    aggregatedData.add(new EnergyData(intervalStart, avgWatts));
                }
            }
            return aggregatedData;
        }

        return rawData;
    }



//    @GetMapping("/meter/latest")
//    public List<EnergyData> getData(
////            @PathVariable String meterNumber,
//            @RequestParam(required = false) LocalDateTime start,
//            @RequestParam(required = false) LocalDateTime end) {
//
////        Meter meter = meterRepository.findByMeterNumber(meterNumber)
////                .orElseThrow(() -> new ZenoApiException("Meter not found: " + meterNumber));
//
//
//        if (start != null && end != null) {
//            return repository.findByTimestampBetween(start, end);
//        }
//        return repository.findAll();
//    }

//    @GetMapping("/meter/{meterNumber}/daily-summary")
//    public List<DailyUsageSummary> getDailySummary(
//            @PathVariable String meterNumber,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
//
//        // Ensure the meter exists (adjust exception type to whatever you use)
//        Meter meter = meterRepository.findByMeterNumber(meterNumber)
//                .orElseThrow(() -> new ZenoApiException("Meter not found: " + meterNumber));
//
//        // Validate time window
//        if (start != null && end != null && start.isAfter(end)) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "start must be before end");
//        }
//
//        // Delegate to repository using the meterNumber
//        return repository.findDailyUsageSummaries(meterNumber, start, end);
//    }
@GetMapping("/average")
@ResponseBody
public ResponseEntity<Map<String, Object>> getAverageWatts(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

    try {
        Double average = powerService.getAverageWatts(start, end);
        Double maximum = powerService.getMaxWatts(start, end);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("start", start);
        response.put("end", end);

        if (average == null) {
            response.put("message", "No data available for the given period");
            return ResponseEntity.ok(response);
        }

        response.put("averageWatts", average);
        response.put("maximumWatts", maximum);

        return ResponseEntity.ok(response);

    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(Collections.singletonMap("error", e.getMessage()));
    }
}
}



