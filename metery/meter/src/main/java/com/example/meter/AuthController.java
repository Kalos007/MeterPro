package com.example.meter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")

public class AuthController {

    @Autowired
    private UserReporsitory userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (userRepository.findByEmail(req.email).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        User user = new User();
        user.setName(req.name);
        user.setEmail(req.email);
        user.setMeterNumber(req.meterNumber);
        user.setPhoneNumber(req.phoneNumber);
        user.setPassword(req.password); // hash this in production
        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Optional<User> userOpt = userRepository.findByEmail(req.email);

        if (userOpt.isEmpty() || !userOpt.get().getPassword().equals(req.password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        User user = userOpt.get();

        // Get token history for this meter
        List<Token> tokens = tokenRepository.findByMeterNumber(user.getMeterNumber());

        // Map user
        UserDTO userDTO = new UserDTO();
        userDTO.name = user.getName();
        userDTO.email = user.getEmail();
        userDTO.meterNumber = user.getMeterNumber();
        userDTO.phoneNumber = user.getPhoneNumber();

        // Map tokens
        List<TokenDTO> tokenDTOs = tokens.stream().map(t -> {
            TokenDTO dto = new TokenDTO();
            dto.code = t.getCode();
            dto.meterNumber = t.getMeterNumber();
            dto.amount = t.getAmount();
//           dto.units = t.getUnits();
            dto.timestamp = String.valueOf(t.getTimestamp());
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(new LoginResponse("Login successful", userDTO, tokenDTOs));
    }
}
