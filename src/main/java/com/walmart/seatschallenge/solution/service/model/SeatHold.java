package com.walmart.seatschallenge.solution.service.model;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "Holds")
@SequenceGenerator(name="holdSeq", initialValue=1, allocationSize=100)
public class SeatHold {

    /**
     * Unique id for the seat
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "holdSeq")
    private long id;

    public String customerEmail;

    /**
     * Date when the seat was held
     */
    private java.sql.Timestamp dateCreated;

    @OneToMany(mappedBy = "seatHold")
    private List<Seat> seats;

    private String confirmationCode;

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public Timestamp getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Timestamp dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }

    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }

}
