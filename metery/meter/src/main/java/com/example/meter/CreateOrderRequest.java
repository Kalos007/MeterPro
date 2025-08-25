package com.example.meter;



import lombok.Data;

@Data
public class CreateOrderRequest {


    private String buyerEmail;


    private String buyerName;



    private String buyerPhone;


    private int amount;

    // @NotBlank(message = "Meter number is required")
    private String meterNumber;
}
