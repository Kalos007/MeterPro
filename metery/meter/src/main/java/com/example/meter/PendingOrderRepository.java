package com.example.meter;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PendingOrderRepository extends JpaRepository<PendingOrder, Long> {
    List<PendingOrder> findAll();
}
