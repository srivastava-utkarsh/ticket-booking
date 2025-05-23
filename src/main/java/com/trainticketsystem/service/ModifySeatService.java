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

	public CompletableFuture<TicketResponse> modifySeating(String userId, String seatNumber) {
		User user = userMap.get(Integer.valueOf(userId));

		if (user.getTicket() != null) {
			Seat seat = seatMap.get(user.getTicket().getSeatNumber());
			if (seat != null) {
				seat.setAvailable(true);
				seat.setReservedBy(null);
			}
		}

		return seatManager.bookSeats(user, seatNumber)
				.thenApply(bookingResult -> {
					if (bookingResult.isSuccess()) {
						return responseBuilder.sendPurchaseSuccessResponse(user, seatNumber);
					}
					return responseBuilder.sendFailedResponse(bookingResult,null);
				})
				.exceptionally(throwable -> {
					log.error("Error processing ticket purchase: {}", throwable.getMessage());
					return responseBuilder.sendFailedResponse(null,"Error processing ticket purchase: " + throwable.getMessage());
				});
	}
}
