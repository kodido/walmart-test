package com.walmart.seatschallenge.solution.persistence;

import com.walmart.seatschallenge.solution.service.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    @Query("SELECT s FROM Seat s WHERE s.seatHold is null")
    List<Seat> findFreeSeats();

}
