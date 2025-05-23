package com.trainticketsystem.service;

import com.trainticketsystem.model.*;
import com.trainticketsystem.response.TicketResponse;
import com.trainticketsystem.utils.TicketResponseBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

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

	public CompletableFuture<TicketResponse> purchaseTicket(String userId, String seatNumber) {
		User user = userMap.get(Integer.valueOf(userId));
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
