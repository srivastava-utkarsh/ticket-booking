package com.trainticketsystem.service;

import com.trainticketsystem.model.User;
import com.trainticketsystem.response.TicketResponse;
import com.trainticketsystem.utils.TicketResponseBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for generating and managing ticket receipts.
 * Handles retrieval of ticket information for receipt generation.
 */
@Service
@Slf4j
public class ReceiptService {

	private final Map<Integer, User> userMap;
	private final TicketResponseBuilder responseBuilder;

	public ReceiptService(Map<Integer, User> userMap, TicketResponseBuilder responseBuilder) {
		this.userMap = userMap;
		this.responseBuilder = responseBuilder;
	}

	/**
	 * Generates a receipt for a user's ticket.
	 * Validates user existence and ticket ownership.
	 * 
	 * @param userId ID of the user to generate receipt for
	 * @return TicketResponse containing ticket details or error message
	 */
	public TicketResponse generateReceipt(String userId) {
		// Get user and log status
		User user = userMap.get(Integer.valueOf(userId));
		log.info("User found: {}", user != null);

		// Validate user exists and has a ticket
		if (user == null || user.getTicket() == null) {
			log.error("User not found or has no ticket for userId: {}", userId);
			return responseBuilder.sendFailedResponse(null, "Ticket not found");
		}

		// Return ticket details in response
		return responseBuilder.sendTicketResponse(user.getTicket());
	}
}
