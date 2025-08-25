package com.example.meter;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
@Table(name="token_tb")
@Data
@Entity
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    @Column(name = "meter_number",unique = true)
    private String meterNumber;

    private double amount;

    private double units;

    private LocalDateTime timestamp;



    // Getters and Setters
}

