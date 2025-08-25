package com.example.meter;

import lombok.Data;

@Data
public class MeterRequest {
    private String type;
    private Double units;
    private Double watts;
    private String action;
}
