# Introduction

This project is my take on the Ticket Service Coding Challenge for Walmart Technology. It is implemented as a SpringBoot application with a simple HTTP API


# Build

From the root of the repository run `./gradlew clean build` (Mac/Linux)
or `gradlew.bat clean build` (Windows) to build the project. This will
download the correct version of Gradle to a cache in your home directory
and execute the `clean` and `build` tasks. 

# Assumptions

The task states that the best available seats should be held for a customer. 
There is no explicit requirement regarding colocation or position of the 
seats in a reservation, so these are not modeled in our solution. A seat in 
our model therefore holds only the following data: 
  - id: unque id of the seat
  - priority: a number, describing how "good" a seat is (lower meaning better)

Another important assumption is that the real high-demand problem is to be handled during hold. 
Once the holds are persisted, converting them to final reservation can be done asyncrhonously 
(returning immediately and notifying the user if the reservation fails for some reason)

# Design Notes

The basic approach approach chosen it to load all seats in a concurrent heap map by priority. This guarantees that the remaining seats will always be returned in the desired order (best first). Important limitation of this design is that it limits the horizontal scalability of the application (since locking happens on in-memory heap structure which is hard to synchronize accross nodes). Decision in favour of this design has been taken since the problem has been defined as  booking seats for a single venue and event with high demand (throughput). As far as throughput is the main concern and the number of seats in real-world venues is limited an in-memory solution should be able to perform well enough on a single node (actual tests show that the biggest existing venues on earth can be booked out in less than 3 seconds on a single node and outdated comodity hardware). 

An alternative approach that has been ignored (because it limits the throughput) is having a clustered solution that places locks on the seats or events (Such locks can be based on some distributed consensus mechanism or central in the storage/DB level, but will definitely perform much worse than a single in-memory structure).
In reality the proposed solution might be required to support multiple venues and events, but in this case sharding different events to different nodes might be appropriate, thus supporting larger volumes of data while still taking advantage of data locality.  

# Data model
The basic model is to maintain seat objects each holding a (potentially null) many-to-one reference to a hold. The hold is a separate object containing the customer email, date and (potentially null) reservation confirmation code. Converting a hold to a reservation means just generating and inserting a confirmation code for the reservation.

# Implementation Notes

Everything relevant to the solution and testing can be found in the following two classes:
  - com.walmart.seatschallenge.solution.service.TicketServiceImpl - contains example implementation
  - com.walmart.seatschallenge.solution.service.TicketServiceImplTest - unit test for the TicketServiceImpl. 

It should be noted that the service returns null in case of failure rathre than throwing an exception. Such behaviour might be considered strange, but is constrained by the interface already defined in the challenge description.

## Persistence
Persistence to a Derby in-memory database has been provided, and some methods have been exposed in a REST resource for illustration purposes only. Proper integration tests on the rest resource are not implemented. It should also be noted that in high-throughput environment persisting to a database directly from the request might be slowing down the system. As an alternative, the entire storage can be pushed outside of the service (by queuing persistence request and executing them asyncrhonously for example). 

## Removing holds
Automatic removal of holds has not yet been implemented, but can be easily done by a periodic scheduled task that just finds all holds that have been created before the specified period and removes them, placing the seats back on the priority queue