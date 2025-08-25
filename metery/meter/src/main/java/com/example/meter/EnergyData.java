package com.example.meter;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Getter
@Table(name="energy_data")
@AllArgsConstructor
@NoArgsConstructor
public class EnergyData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @ManyToOne(optional = false)
//    @JoinColumn(name = "meter_id")
//    private Meter meter;
    private float units;
    private Long watts;
    private LocalDateTime timestamp;

    public EnergyData(LocalDateTime timestamp, Long watts) {
        this.timestamp = timestamp;
        this.watts = watts;
        ; // Set to null as it's not used in aggregation
    }

    // Constructors, Getters, Setters
}