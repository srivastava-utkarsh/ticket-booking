package com.trainticketsystem.service;

import com.trainticketsystem.model.User;
import com.trainticketsystem.response.TicketResponse;
import com.trainticketsystem.utils.TicketResponseBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for handling ticket purchase operations.
 * Manages the process of booking seats for users with validation and error handling.
 */
@Service
@Slf4j
public class PurchaseTicketService {

	private final SeatManager seatManager;
	private final Map<Integer, User> userMap;
	private final TicketResponseBuilder responseBuilder;

	public PurchaseTicketService(SeatManager seatManager, Map<Integer, User> userMap, TicketResponseBuilder responseBuilder) {
		this.seatManager = seatManager;
		this.userMap = userMap;
		this.responseBuilder = responseBuilder;
	}

	/**
	 * Purchases a ticket for a user.
	 * Validates user existence and current ticket status before booking.
	 * 
	 * @param userId ID of the user purchasing the ticket
	 * @param seatNumber Seat number to be booked
	 * @return CompletableFuture with booking result or error message
	 */
	public CompletableFuture<TicketResponse> purchaseTicket(String userId, String seatNumber) {
		// Get user and validate existence
		User user = userMap.get(Integer.valueOf(userId));
		if(user == null)
			return CompletableFuture.completedFuture(responseBuilder.sendFailedResponse(null, "User not found"));

		// Check if user already has a ticket
		if(user.getTicket() != null)
			return CompletableFuture.completedFuture(responseBuilder.sendFailedResponse(null, "User already holds ticket " + user.getTicket().getSeatNumber()));

		// Attempt to book the seat
		return seatManager.bookSeats(user, seatNumber)
				.thenApply(bookingResult -> {
					// Handle successful booking
					if (bookingResult.isSuccess()) {
						return responseBuilder.sendPurchaseSuccessResponse(user, seatNumber);
					}
					// Handle booking failure
					return responseBuilder.sendFailedResponse(bookingResult, null);
				})
				.exceptionally(throwable -> {
					// Handle any errors during booking
					log.error("Error processing ticket purchase: {}", throwable.getMessage());
					return responseBuilder.sendFailedResponse(null, "Error processing ticket purchase: " + throwable.getMessage());
				});
	}
}
