package com.example.meter;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OrderStatusResponse {
    private String status;

    @JsonProperty("order_id")
    private String orderId;

    private String message;

    private String amount;

    @JsonProperty("payment_status")
    private String paymentStatus;

    @Override
    public String toString() {
        return "OrderStatusResponse{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", orderId='" + orderId + '\'' +
                ", amount='" + amount + '\'' +
                ", paymentStatus='" + paymentStatus + '\'' +
                '}';
    }
}
