package com.walmart.seatschallenge.solution.service;

import com.walmart.seatschallenge.solution.persistence.SeatHoldRepository;
import com.walmart.seatschallenge.solution.persistence.SeatRepository;
import com.walmart.seatschallenge.solution.service.model.Seat;
import com.walmart.seatschallenge.solution.service.model.SeatHold;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    SeatRepository seatRepository;

    @Autowired
    SeatHoldRepository seatHoldRepository;


    /**
     * A heap keeping the seats in order of their scores
     */
    private PriorityBlockingQueue<Seat> availableSeats;

    private static class ConfirmationCodeGenerator {
        private static int CONFIRMATION_CODE_LENGTH = 10;
        static final private String SYMBOLS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        private String generateConfirmationCode(SeatHold seatHold) {
            Random random = new Random(seatHold.getId());
            StringBuilder sb = new StringBuilder(CONFIRMATION_CODE_LENGTH);
            for (int i = 0; i < CONFIRMATION_CODE_LENGTH; ++i) {
                sb.append(SYMBOLS.charAt(random.nextInt(SYMBOLS.length())));
            }
            return sb.toString();
        }
    }

    ConfirmationCodeGenerator confirmationCodeGenerator = new ConfirmationCodeGenerator();

    @PostConstruct
    void init() {
        List<Seat> freeSeats = seatRepository.findFreeSeats();
        this.availableSeats = new PriorityBlockingQueue<>(
                freeSeats.size() > 0 ? freeSeats.size() : 10,
                Comparator.comparing(Seat::getPriority));
        freeSeats.forEach(availableSeats::add);
    }

    @Override
    public int numSeatsAvailable() {
        return availableSeats.size();
    }

    @Override
    public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
        if (availableSeats.size() < numSeats) {
            return null;
        }
        List<Seat> reservedSeats = new ArrayList<>(numSeats);
        availableSeats.drainTo(reservedSeats, numSeats);
        if (reservedSeats.size() < numSeats) {
            return null;
        }
        return holdSeats(reservedSeats, customerEmail);
    }

    @Override
    public String reserveSeats(int seatHoldId, String customerEmail) {
        Optional<SeatHold> seatHold = seatHoldRepository.findById(Long.valueOf(seatHoldId));
        if (seatHold.isPresent()) {
            SeatHold hold = seatHold.get();
            if (hold.getCustomerEmail() != customerEmail) {
                return null;
            }
            hold.setConfirmationCode(confirmationCodeGenerator.generateConfirmationCode(hold));
            seatHoldRepository.save(hold);
            return hold.getConfirmationCode();
        } else {
            return null;
        }
    }

    private SeatHold holdSeats(List<Seat> reservedSeats, String customerEmail) {
        SeatHold hold = new SeatHold();
        hold.setCustomerEmail(customerEmail);
        hold.setSeats(reservedSeats);
        /* Might introduce time zone issues, should normally be converted/stored as UTC */
        hold.setDateCreated(new Timestamp(System.currentTimeMillis()));
        seatHoldRepository.save(hold);
        return hold;
    }

    @Scheduled(fixedRate = 5000)
    public void clearExpiredHolds() {
        /**
         * Not implemented in this example, but generally just deletes all the locks that have
         * creationtime <= now - hold_expiration_time and puts them back in the queue
         */
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
