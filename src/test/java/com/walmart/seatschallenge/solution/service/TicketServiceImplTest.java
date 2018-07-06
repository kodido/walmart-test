package com.walmart.seatschallenge.solution.service;

import com.walmart.seatschallenge.solution.persistence.SeatHoldRepository;
import com.walmart.seatschallenge.solution.persistence.SeatRepository;
import com.walmart.seatschallenge.solution.service.model.Seat;
import com.walmart.seatschallenge.solution.service.model.SeatHold;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class TicketServiceImplTest {

    private AtomicLong seatHoldIdGen = new AtomicLong();
    private TicketService instance;
    private Map<Long, Seat> seatMap;

    // the number of seats at the biggest venue on earth
    private static int INDIANAPOLIS_MOTOR_SPEEDWAY_SEAT_COUNT = 257325;

    @Before
    public void before() {
        TicketServiceImpl ticketService = new TicketServiceImpl();
        seatMap = generateSeats(INDIANAPOLIS_MOTOR_SPEEDWAY_SEAT_COUNT);
        ticketService.seatRepository = mockSeatRepository();
        ticketService.seatHoldRepository = mockSeatHoldRepository();
        ticketService.init();
        this.instance = ticketService;
    }

    private Map<Long, Seat> generateSeats(long count) {
        return LongStream.range(1, count + 1).mapToObj(number -> {
            Seat seat = new Seat();
            seat.setId(number);
            seat.setPriority(number/10 + 1);
            return seat;
        }).collect(Collectors.toMap(Seat::getId, Function.identity()));
    }

    private SeatHoldRepository mockSeatHoldRepository() {
        SeatHoldRepository result = Mockito.mock(SeatHoldRepository.class);
        Mockito.when(result.save(Mockito.any())).thenAnswer(invocation -> {
            synchronized (this) {
                SeatHold arg = invocation.getArgument(0);
                arg.setId(seatHoldIdGen.incrementAndGet());
                for (Seat s : arg.getSeats()) {
                    seatMap.get(s.getId()).setSeatHold(arg);
                }
                return arg;
            }
        });
        return result;
    }

    private SeatRepository mockSeatRepository() {
        SeatRepository result = Mockito.mock(SeatRepository.class);
        Mockito.when(result.findFreeSeats()).thenReturn(findAvailableSeats());
        return result;
    }

    private synchronized List<Seat> findAvailableSeats() {
        return seatMap.entrySet().stream().map(Map.Entry::getValue).filter(s -> s.getSeatHold() == null).collect(Collectors.toList());
    }

    @Test
    public void testNumberOfUnoccupiedSeatsEqualsNumberOfSeatsWithNoHold() {
        Assert.assertEquals(findAvailableSeats().size(), instance.numSeatsAvailable());
    }

    @Test
    public void testHoldMoreSeatsThanAvailable() {
        SeatHold seatHold = instance.findAndHoldSeats(seatMap.size() + 1, "test@test.com");
        Assert.assertNull(seatHold);
    }

    @Test
    public void testHold3Seats() {
        int initialSeatSize = findAvailableSeats().size();
        SeatHold seatHold = instance.findAndHoldSeats(3, "test@test.com");
        Assert.assertNotNull(seatHold);
        Assert.assertEquals(initialSeatSize - 3, findAvailableSeats().size());

        // check that the 3 highest scores got reserved
        List<Long> reservedScores = seatHold.getSeats().stream().map(Seat::getPriority).sorted().collect(Collectors.toList());
        Assert.assertEquals(3, reservedScores.size());
        Assert.assertEquals(1, (long)reservedScores.get(0));
        Assert.assertEquals(1, (long)reservedScores.get(1));
        Assert.assertEquals(1, (long)reservedScores.get(2));
    }

    @Test
    public void holdTheIndianopolisMotorSpeedway() throws InterruptedException, ExecutionException {
        Random random = new Random();

        // a callable reserving random number of threads between 1 and 10
        Callable<SeatHold> task = () ->
                instance.findAndHoldSeats(random.nextInt(10) + 1, "blahblah@blah.com");

        int threadCount = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        List<Future<SeatHold>> futures = new LinkedList<>();
        do {
            Collections.nCopies(threadCount, task);
            futures.addAll(executorService.invokeAll(Collections.nCopies(threadCount, task)));
        } while (instance.numSeatsAvailable() > 0);

        Set<Seat> reservedSeatsSet = new HashSet<>();
        for (Future<SeatHold> future : futures) {
            SeatHold hold = future.get();
            if (hold != null) {
                System.out.println("Reservation for " + hold.getSeats().size());
                for (Seat reservedSeat : hold.getSeats()) {
                    Assert.assertTrue(reservedSeatsSet.add(reservedSeat));
                }
            }
        }
        Assert.assertEquals(INDIANAPOLIS_MOTOR_SPEEDWAY_SEAT_COUNT, reservedSeatsSet.size());
        System.out.println("Total number of seats reserved" + reservedSeatsSet);

    }



}
