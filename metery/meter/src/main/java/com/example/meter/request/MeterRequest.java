package com.example.meter.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
@AllArgsConstructor
public class MeterRequest {
    private String type;
    private Double units;
    private String action;
    private double watts;
}
