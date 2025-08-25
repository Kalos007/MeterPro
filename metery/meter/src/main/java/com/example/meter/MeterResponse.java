package com.example.meter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@AllArgsConstructor
public class MeterResponse {
    // Standard getters and setters
    private String status;     // "success" or "error"
    @Setter
    private String message;    // Descriptive message
    private Object data;       // Response payload

    public MeterResponse(String status) {
    }
}
