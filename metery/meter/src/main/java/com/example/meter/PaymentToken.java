package com.example.meter;

import jdk.jfr.DataAmount;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;



@Data
@AllArgsConstructor

public class PaymentToken {
    // Standard getters and setters
    private String code;         // Unique token code
    private String meterNumber;  // Associated meter number
    private double amount;       // Payment amount
    private double units; // Units purchased
    private String timestamp;    // When payment occurred

    public PaymentToken() {

    }
}

