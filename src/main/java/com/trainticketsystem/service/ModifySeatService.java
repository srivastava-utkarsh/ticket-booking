package com.trainticketsystem.service;

import com.trainticketsystem.model.Seat;
import com.trainticketsystem.model.SeatManager;
import com.trainticketsystem.model.User;
import com.trainticketsystem.response.TicketResponse;
import com.trainticketsystem.utils.TicketResponseBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for handling seat modification operations.
 * Manages the process of changing a user's seat with validation and cleanup.
 */
@Service
@Slf4j
public class ModifySeatService {

	private final SeatManager seatManager;
	private final Map<Integer, User> userMap;
	private final TicketResponseBuilder responseBuilder;
	private final Map<String, Seat> seatMap;

	public ModifySeatService(SeatManager seatManager,
	                         Map<Integer, User> userMap,
	                         TicketResponseBuilder responseBuilder,
	                         Map<String, Seat> seatMap) {
		this.seatManager = seatManager;
		this.userMap = userMap;
		this.responseBuilder = responseBuilder;
		this.seatMap = seatMap;
	}

	/**
	 * Modifies a user's seat assignment.
	 * Releases the old seat and books a new one if available.
	 * 
	 * @param userId ID of the user modifying their seat
	 * @param seatNumber New seat number to be booked
	 * @return CompletableFuture with modification result or error message
	 */
	public CompletableFuture<TicketResponse> modifySeating(String userId, String seatNumber) {
		// Get user from map
		User user = userMap.get(Integer.valueOf(userId));

		// Check if user has existing ticket
		if (user.getTicket() != null) {
			// Prevent booking same seat again
			if(user.getTicket().getSeatNumber().equals(seatNumber))
				return CompletableFuture.completedFuture(responseBuilder.sendFailedResponse(null, "Cannot book same ticket again"));

			// Release old seat
			Seat seat = seatMap.get(user.getTicket().getSeatNumber());
			if (seat != null) {
				seat.setAvailable(true);
				seat.setReservedBy(null);
			}
		}

		// Book new seat
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
