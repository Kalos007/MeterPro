package com.example.meter;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "meters")
@Data
@Getter
@Setter
  // allow null
public class Meter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

    @Column(name = "meter_number", nullable = false, unique = true)
    private String meterNumber;
    private double units;
    private String bulb ;
    private double watts;

    // Getters and Setters
}

