package com.example.meter;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
public class EnergyDataDto {
    private LocalDateTime timestamp;
    private long watts;

    // constructors, getters, setters
}

