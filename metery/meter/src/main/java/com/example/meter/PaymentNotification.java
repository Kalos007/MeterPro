package com.example.meter;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
class PaymentNotification {
    // Standard getters and setters
    private String meterNumber;  // Identifier for the meter
    private double amount;       // Payment amount
    private String status;
//private String orderId;// Payment status

}
