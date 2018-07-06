package com.walmart.seatschallenge.solution.persistence;

import com.walmart.seatschallenge.solution.service.model.SeatHold;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatHoldRepository extends JpaRepository<SeatHold, Long> {
}
