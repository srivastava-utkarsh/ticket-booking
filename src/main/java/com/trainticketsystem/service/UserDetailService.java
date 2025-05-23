package com.trainticketsystem.service;

import com.trainticketsystem.model.Seat;
import com.trainticketsystem.model.User;
import com.trainticketsystem.response.TicketResponse;
import com.trainticketsystem.utils.TicketResponseBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class UserDetailService {

	private final Map<Integer, User> userMap;
	private final TicketResponseBuilder responseBuilder;
	private final Map<String, Seat> seatMap;

	public UserDetailService(Map<Integer, User> userMap,
	                         TicketResponseBuilder responseBuilder,
	                         Map<String, Seat> seatMap) {
		this.userMap = userMap;
		this.responseBuilder = responseBuilder;
		this.seatMap = seatMap;
	}

	public TicketResponse getUserDetails(String userId){
		User user = userMap.get(Integer.valueOf(userId));
		log.info("User found: {}", user != null);

		if (user == null || user.getTicket() == null) {
			log.error("User not found or has no ticket for userId: {}", userId);
			return responseBuilder.sendFailedResponse(null,"User not found");
		}

		return responseBuilder.sendUserResponse(user);
	}

	public void deleteUser(String userId){
		User user = userMap.get(Integer.valueOf(userId));
		log.info("User found: {}", user != null);
		if (user == null) {
			log.error("User not found or has no ticket for userId: {}", userId);
		}
		userMap.remove(Integer.valueOf(userId));
		// Free up the seat if user had one
		if (user.getTicket() != null) {
			Seat seat = seatMap.get(user.getTicket().getSeatNumber());
			if (seat != null) {
				seat.setAvailable(true);
				seat.setReservedBy(null);
			}
		}
	}
}
