package com.example.meter;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;


@Getter
@RequiredArgsConstructor
public class DailyUsageSummary {
    private LocalDate date;
    private Double avgWatts;
    private Double peakWatts;

    public DailyUsageSummary(LocalDate date, Double avgUsage, Double peakUsage) {
        this.date = date;
        this.avgWatts = avgWatts;
        this.peakWatts = peakWatts;
    }

}
