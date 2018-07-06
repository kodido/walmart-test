package com.walmart.seatschallenge.solution.service.model;

import javax.persistence.*;

@Entity
@Table(name = "Seats")
@SequenceGenerator(name="seatSeq", initialValue=1, allocationSize=100)
public class Seat {

    /**
     * Unique id for the seat
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seatSeq")
    private long id;

    /**
     * Reflects the "desirability" of the seat, the less the more desired
     */
    private long priority;

    @ManyToOne
    private SeatHold seatHold;

    public SeatHold getSeatHold() {
        return seatHold;
    }

    public void setSeatHold(SeatHold seatHold) {
        this.seatHold = seatHold;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPriority() {
        return priority;
    }

    public void setPriority(long priority) {
        this.priority = priority;
    }
}
