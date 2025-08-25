package com.example.meter;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TokenDTO {
    public String code;
    public String meterNumber;
    public double amount;
    public double units;
    public String timestamp;
}
