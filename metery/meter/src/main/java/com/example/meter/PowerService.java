package com.example.meter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PowerService {

    @Autowired
    private EnergyDataRepository energyDataRepository;


    public Double getAverageWatts(LocalDateTime start, LocalDateTime end){
        return energyDataRepository.findAverageWattsBetween(start, end);
    }

    public Double getMaxWatts(LocalDateTime start, LocalDateTime end) {
        return energyDataRepository.findMaxWattsBetween(start,end);
    }
}
