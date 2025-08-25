package com.example.meter;

import java.util.List;

public class LoginResponse {
    public String message;
    public UserDTO user;
    public List<TokenDTO> tokens;

    public LoginResponse(String message, UserDTO user, List<TokenDTO> tokens) {
        this.message = message;
        this.user = user;
        this.tokens = tokens;
    }
}
