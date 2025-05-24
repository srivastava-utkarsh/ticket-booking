package com.trainticketsystem.service;

import com.trainticketsystem.config.AppProperties;
import com.trainticketsystem.model.BookingResult;
import com.trainticketsystem.model.Seat;
import com.trainticketsystem.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class SeatManagerTest {

    @Mock
    private AppProperties appProperties;

    private Map<String, Seat> seatMap;
    private SeatManager seatManager;
    private static final int NUM_USERS = 10;
    private static final int TICKET_PRICE = 100;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        seatMap = new ConcurrentHashMap<>();
        
        // Initialize seats
        for (int i = 1; i <= 5; i++) {
            String seatIdA = "A" + i;
            String seatIdB = "B" + i;
            seatMap.put(seatIdA, new Seat(seatIdA, true));
            seatMap.put(seatIdB, new Seat(seatIdB, true));
        }

        when(appProperties.getTicketPrice()).thenReturn(TICKET_PRICE);
        seatManager = new SeatManager(seatMap, appProperties);
    }

    @Test
    void testBasicSeatBooking() {
        // Create a test user
        User user = User.builder()
                .id(1)
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .walletBalance(200)
                .build();

        // Try to book seat A1
        CompletableFuture<BookingResult> future = seatManager.bookSeats(user, "A1");
        BookingResult result = future.join();

        // Verify booking was successful
        assertTrue(result.isSuccess());
        assertFalse(seatMap.get("A1").isAvailable());
        assertEquals(user, seatMap.get("A1").getReservedBy());
    }

    @Test
    void testConcurrentDifferentSeatBookings() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(NUM_USERS);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // Create and book seats for 10 users
        for (int i = 1; i <= NUM_USERS; i++) {
            final int userId = i;
            final String seatId = i <= 5 ? "A" + i : "B" + (i - 5);
            
            User user = User.builder()
                    .id(userId)
                    .firstName("User" + userId)
                    .lastName("Test" + userId)
                    .email("user" + userId + "@test.com")
                    .walletBalance(200)
                    .build();

            CompletableFuture.runAsync(() -> {
                try {
                    CompletableFuture<BookingResult> future = seatManager.bookSeats(user, seatId);
                    BookingResult result = future.get(5, TimeUnit.SECONDS);
                    
                    if (result.isSuccess()) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all bookings to complete
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        assertEquals(NUM_USERS, successCount.get());
        assertEquals(0, failureCount.get());
    }

    @Test
    void testConcurrentSameSeatBookings() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(NUM_USERS);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        final String targetSeat = "A1";

        // Create 10 users trying to book the same seat
        for (int i = 1; i <= NUM_USERS; i++) {
            final int userId = i;
            User user = User.builder()
                    .id(userId)
                    .firstName("User" + userId)
                    .lastName("Test" + userId)
                    .email("user" + userId + "@test.com")
                    .walletBalance(200)
                    .build();

            CompletableFuture.runAsync(() -> {
                try {
                    CompletableFuture<BookingResult> future = seatManager.bookSeats(user, targetSeat);
                    BookingResult result = future.get(5, TimeUnit.SECONDS);
                    
                    if (result.isSuccess()) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all bookings to complete
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        assertEquals(1, successCount.get()); // Only one booking should succeed
        assertEquals(NUM_USERS - 1, failureCount.get()); // Rest should fail
    }
} 