package com.trainticketsystem.service;

import com.trainticketsystem.model.User;
import com.trainticketsystem.response.TicketResponse;
import com.trainticketsystem.utils.TicketResponseBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class ReceiptService {

	private final Map<Integer, User> userMap;
	private final TicketResponseBuilder responseBuilder;

	public ReceiptService(Map<Integer, User> userMap, TicketResponseBuilder responseBuilder) {
		this.userMap = userMap;
		this.responseBuilder = responseBuilder;
	}

	public TicketResponse generateReceipt(String userId){
		User user = userMap.get(Integer.valueOf(userId));
		log.info("User found: {}", user != null);

		if (user == null || user.getTicket() == null) {
			log.error("User not found or has no ticket for userId: {}", userId);
			return responseBuilder.sendFailedResponse(null,"Ticket not found");
		}

		return responseBuilder.sendTicketResponse(user.getTicket());
	}
}
