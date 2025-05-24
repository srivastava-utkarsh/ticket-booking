package com.trainticketsystem.service;

import com.trainticketsystem.model.BookingResult;
import com.trainticketsystem.model.User;
import com.trainticketsystem.response.TicketResponse;
import com.trainticketsystem.utils.TicketResponseBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Test class for PurchaseTicketService focusing on concurrent booking scenarios.
 * Simulates multiple users trying to book seats simultaneously.
 */
class PurchaseTicketServiceTest {

    @Mock
    private SeatManager seatManager;

    @Mock
    private TicketResponseBuilder responseBuilder;

    private Map<Integer, User> userMap;
    private PurchaseTicketService purchaseTicketService;
    private ExecutorService executorService;
    private static final int NUM_USERS = 10;
    private static final int NUM_THREADS = 10;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userMap = new ConcurrentHashMap<>();
        purchaseTicketService = new PurchaseTicketService(seatManager, userMap, responseBuilder);
        executorService = Executors.newFixedThreadPool(NUM_THREADS);

        // Initialize test users
        for (int i = 1; i <= NUM_USERS; i++) {
            User user = User.builder()
                    .id(i)
                    .firstName("User" + i)
                    .lastName("Test" + i)
                    .email("user" + i + "@test.com")
                    .walletBalance(100)
                    .build();
            userMap.put(i, user);
        }
    }

    /**
     * Test concurrent booking of different seats.
     * Simulates 10 users trying to book different seats simultaneously.
     */
    @Test
    void testConcurrentDifferentSeatBookings() throws InterruptedException {
        // Setup
        CountDownLatch latch = new CountDownLatch(NUM_USERS);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // Mock successful booking response
        TicketResponse successResponse = TicketResponse.builder()
                .transactionStatus(true)
                .message("Ticket purchased successfully")
                .build();
        when(responseBuilder.sendPurchaseSuccessResponse(any(User.class), anyString()))
                .thenReturn(successResponse);

        // Mock successful booking result
        when(seatManager.bookSeats(any(User.class), anyString()))
                .thenReturn(CompletableFuture.completedFuture(BookingResult.success()));

        // Simulate concurrent bookings
        for (int i = 1; i <= NUM_USERS; i++) {
            final int userId = i;
            final String seatNumber = "A" + i; // Each user tries to book a different seat
            executorService.submit(() -> {
                try {
                    CompletableFuture<TicketResponse> future = purchaseTicketService.purchaseTicket(
                            String.valueOf(userId), seatNumber);
                    TicketResponse response = future.get(5, TimeUnit.SECONDS);
                    if (response.isTransactionStatus()) {
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
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(NUM_USERS, successCount.get());
        assertEquals(0, failureCount.get());
    }

    /**
     * Test concurrent booking of same seats.
     * Simulates 10 users trying to book the same seat simultaneously.
     */
    @Test
    void testConcurrentSameSeatBookings() throws InterruptedException {
        // Setup
        CountDownLatch latch = new CountDownLatch(NUM_USERS);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        final String targetSeat = "A1";

        // Mock responses
        TicketResponse successResponse = TicketResponse.builder()
                .transactionStatus(true)
                .message("Ticket purchased successfully")
                .build();
        TicketResponse failureResponse = TicketResponse.builder()
                .transactionStatus(false)
                .message("Seat already booked")
                .build();

        // Mock booking results - only first booking succeeds
        when(seatManager.bookSeats(any(User.class), eq(targetSeat)))
                .thenReturn(CompletableFuture.completedFuture(BookingResult.success()))
                .thenReturn(CompletableFuture.completedFuture(BookingResult.failed("Seat already booked")));

        when(responseBuilder.sendPurchaseSuccessResponse(any(User.class), eq(targetSeat)))
                .thenReturn(successResponse);
        when(responseBuilder.sendFailedResponse(any(BookingResult.class), anyString()))
                .thenReturn(failureResponse);

        // Simulate concurrent bookings of same seat
        for (int i = 1; i <= NUM_USERS; i++) {
            final int userId = i;
            executorService.submit(() -> {
                try {
                    CompletableFuture<TicketResponse> future = purchaseTicketService.purchaseTicket(
                            String.valueOf(userId), targetSeat);
                    TicketResponse response = future.get(5, TimeUnit.SECONDS);
                    if (response.isTransactionStatus()) {
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

    /**
     * Test concurrent booking with mixed scenarios.
     * Some users try to book same seats, others try different seats.
     */
    @Test
    void testMixedConcurrentBookings() throws InterruptedException {
        // Setup
        CountDownLatch latch = new CountDownLatch(NUM_USERS);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // Mock responses
        TicketResponse successResponse = TicketResponse.builder()
                .transactionStatus(true)
                .message("Ticket purchased successfully")
                .build();
        TicketResponse failureResponse = TicketResponse.builder()
                .transactionStatus(false)
                .message("Seat already booked")
                .build();

        // Mock booking results
        when(seatManager.bookSeats(any(User.class), anyString()))
                .thenAnswer(invocation -> {
                    String seatNumber = invocation.getArgument(1);
                    // Allow only one booking per seat
                    if (seatNumber.equals("A1") || seatNumber.equals("B1")) {
                        return CompletableFuture.completedFuture(BookingResult.success());
                    }
                    return CompletableFuture.completedFuture(BookingResult.failed("Seat already booked"));
                });

        when(responseBuilder.sendPurchaseSuccessResponse(any(User.class), anyString()))
                .thenReturn(successResponse);
        when(responseBuilder.sendFailedResponse(any(BookingResult.class), anyString()))
                .thenReturn(failureResponse);

        // Simulate mixed concurrent bookings
        for (int i = 1; i <= NUM_USERS; i++) {
            final int userId = i;
            final String seatNumber = i % 2 == 0 ? "A1" : "B1"; // Alternate between A1 and B1
            executorService.submit(() -> {
                try {
                    CompletableFuture<TicketResponse> future = purchaseTicketService.purchaseTicket(
                            String.valueOf(userId), seatNumber);
                    TicketResponse response = future.get(5, TimeUnit.SECONDS);
                    if (response.isTransactionStatus()) {
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
        assertEquals(2, successCount.get()); // Only one booking per seat should succeed
        assertEquals(NUM_USERS - 2, failureCount.get()); // Rest should fail
    }
} 