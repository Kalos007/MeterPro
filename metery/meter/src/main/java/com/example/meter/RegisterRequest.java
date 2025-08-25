package com.example.meter;

import lombok.Data;

@Data
public class RegisterRequest {
    public String name;
    public String email;
    public String meterNumber;
    public String phoneNumber;
    public String password;
}
