package com.example.meter;

import lombok.Data;

@Data
public class PostUnitRequest {
    private String meterNumber;
    private Double units;
    private Integer watts;

}

