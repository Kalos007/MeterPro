package com.example.meter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserReporsitory extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByMeterNumber(String meterNumber);
}

