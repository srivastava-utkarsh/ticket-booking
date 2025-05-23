package com.trainticketsystem.model;

import com.trainticketsystem.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Manages seat booking operations with thread safety and timeout handling.
 * Handles seat reservation, payment processing, and error recovery.
 */
@Service
@Slf4j
public class SeatManager {

	private final Map<String, Seat> seatMap;
	private final AppProperties appProperties;

	@Autowired
	public SeatManager(Map<String, Seat> seatMap, AppProperties appProperties) {
		this.seatMap = seatMap;
		this.appProperties = appProperties;
	}

	/**
	 * Books a seat for a user with the following steps:
	 * 1. Validates seat existence
	 * 2. Acquires seat lock with timeout
	 * 3. Checks seat availability
	 * 4. Processes payment
	 * 5. Reserves seat
	 * 6. Handles errors and refunds if needed
	 * 
	 * @param user User booking the seat
	 * @param seatId Seat to be booked
	 * @return CompletableFuture with booking result
	 */
	public CompletableFuture<BookingResult> bookSeats(User user, String seatId) {
		return CompletableFuture.supplyAsync(() -> {
			// Validate seat exists
			Seat seat = seatMap.get(seatId);
			if (seat == null) {
				return BookingResult.failed("Seat not found: " + seatId);
			}

			try {
				// Try to acquire lock with timeout
				if (!seat.getLock().tryLock(2, TimeUnit.SECONDS)) {
					return BookingResult.failed("Seat " + seatId + " is busy. Try again later.");
				}

				try {
					// Check seat availability
					if (!seat.isAvailable()) {
						return BookingResult.failed("Seat " + seatId + " is already booked");
					}

					// Process payment
					if (!user.deductBalance(appProperties.getTicketPrice())) {
						return BookingResult.failed("Insufficient balance");
					}

					try {
						// Book the seat
						if (!seat.reserve(user)) {
							// If reservation fails, refund the payment
							user.addBalance(appProperties.getTicketPrice());
							return BookingResult.failed("Failed to reserve seat " + seatId);
						}

						log.info("Successfully booked seat {} for user {}", seatId, user.getEmail());
						return BookingResult.success();
					} catch (Exception e) {
						// If any error occurs during reservation, refund the payment
						user.addBalance(appProperties.getTicketPrice());
						log.error("Error during seat reservation: {}", e.getMessage());
						return BookingResult.failed("Error during seat reservation: " + e.getMessage());
					}
				} finally {
					seat.getLock().unlock();
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return BookingResult.failed("Thread interrupted during booking");
			}
		}).orTimeout(5, TimeUnit.SECONDS)  // Overall timeout for the entire operation
		  .exceptionally(throwable -> {
			  if (throwable instanceof TimeoutException) {
				  log.error("Booking operation timed out for user {} and seat {}", user.getEmail(), seatId);
				  return BookingResult.failed("Booking operation timed out. Please try again.");
			  }
			  log.error("Unexpected error during booking: {}", throwable.getMessage());
			  return BookingResult.failed("Unexpected error during booking: " + throwable.getMessage());
		  });
	}
}
