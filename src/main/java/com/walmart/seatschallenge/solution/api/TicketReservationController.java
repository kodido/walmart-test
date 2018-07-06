package com.walmart.seatschallenge.solution.api;

import com.walmart.seatschallenge.solution.api.model.NewReservationPayload;
import com.walmart.seatschallenge.solution.api.model.NewSeatsHoldPayload;
import com.walmart.seatschallenge.solution.service.TicketService;
import com.walmart.seatschallenge.solution.service.model.SeatHold;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/seats")
public class TicketReservationController {


    @Autowired
    private TicketService ticketService;

    @PostMapping("/hold")
    public Mono<SeatHold> holdSeats(@RequestBody NewSeatsHoldPayload payload) {
        return Mono.fromCallable(() -> ticketService.findAndHoldSeats(payload.getNumberOfSeats(), payload.getCustomerEmail()));
    }

    @PostMapping("/reserve")
    public Mono<String> reserveSeats(@RequestBody NewReservationPayload payload) {
        return Mono.fromCallable(() -> ticketService.reserveSeats(payload.getHoldId(), payload.getCustomerEmail()));
    }

    @GetMapping("/available")
    public Mono<Integer> getAvailableSeats() {
        return Mono.fromCallable(ticketService::numSeatsAvailable);
    }

}
