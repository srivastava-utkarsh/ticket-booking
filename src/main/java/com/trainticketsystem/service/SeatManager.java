package com.trainticketsystem.service;

import com.trainticketsystem.config.AppProperties;
import com.trainticketsystem.model.BookingResult;
import com.trainticketsystem.model.Seat;
import com.trainticketsystem.model.User;
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
			log.info("entered bookSeats with user {} for seat {}",user.getId(),seatId);
			// Validate seat exists
			Seat seat = seatMap.get(seatId);
			if (seat == null) {
				log.info("bookSeats::seat not found for user {} and seat {}",user.getId(),seatId);
				return BookingResult.failed("Seat not found: " + seatId);
			}

			try {
				// Try to acquire lock with timeout
				if (!seat.getLock().tryLock(2, TimeUnit.SECONDS)) {
					log.info("bookSeats::!seat.getLock()::user {} , seat {} is busy. Try again later",user.getId(),seatId);
					return BookingResult.failed("Seat " + seatId + " is busy. Try again later.");
				}

				try {
					// Check seat availability
					if (!seat.isAvailable()) {
						log.info("bookSeats::!seat.isAvailable()::user {} , seat {} is already booked",user.getId(),seatId);
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
							log.info("bookSeats::!seat.reserve(user)::user {} , Failed to reserve seat {}",user.getId(),seatId);
							user.addBalance(appProperties.getTicketPrice());
							return BookingResult.failed("Failed to reserve seat " + seatId);
						}

						log.info("Successfully booked seat {} for user {}", seatId, user.getId());
						return BookingResult.success();
					} catch (Exception e) {
						// If any error occurs during reservation, refund the payment
						user.addBalance(appProperties.getTicketPrice());
						log.info("Error during seat reservation: {} , for user {}", e.getMessage(),user.getId());
						return BookingResult.failed("Error during seat reservation: " + e.getMessage());
					}
				} finally {
					seat.getLock().unlock();
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				log.info("InterruptedException {}::user {} , seat {}", e.getMessage(),user.getId(),seatId);
				return BookingResult.failed("Thread interrupted during booking");
			}
		}).orTimeout(5, TimeUnit.SECONDS)  // Overall timeout for the entire operation
		  .exceptionally(throwable -> {
			  if (throwable instanceof TimeoutException) {
				  log.info("Booking operation timed out for user {} and seat {}", user.getEmail(), seatId);
				  return BookingResult.failed("Booking operation timed out. Please try again.");
			  }
			  log.info("Unexpected error during booking: {} , for user {} and seat {}", throwable.getMessage(),user.getEmail(), seatId);
			  return BookingResult.failed("Unexpected error during booking: " + throwable.getMessage());
		  });
	}
}
