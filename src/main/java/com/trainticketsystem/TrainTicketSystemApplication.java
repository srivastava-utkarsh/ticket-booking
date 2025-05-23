package com.trainticketsystem;

import com.trainticketsystem.model.User;
import com.trainticketsystem.service.PurchaseTicketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Map;

@SpringBootApplication
@Slf4j
public class TrainTicketSystemApplication implements CommandLineRunner {

	private final Map<Integer, User> userMap;
	private final PurchaseTicketService purchaseTicketService;

	@Autowired
	public TrainTicketSystemApplication(Map<Integer, User> userMap, PurchaseTicketService purchaseTicketService) {
		this.userMap = userMap;
		this.purchaseTicketService = purchaseTicketService;
	}

	public static void main(String[] args) {
		SpringApplication.run(TrainTicketSystemApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// List all available users
		log.info("Available users:");
//		userMap.forEach((email, user) ->
//			log.info("User: {}, Balance: {}", email, user.getWalletBalance())
//		);
//
//		// Get a specific user
//		User user = userMap.get("user_1@test.com");
//		if (user != null) {
//			log.info("Found user: {}", user.getEmail());
//
//			// Example: Try to book a seat for this user
//			trainService.purchaseTicket("user_1@test.com", "A1")
//					.thenAccept(response -> {
//						if (response.isBookingSuccess()) {
//							log.info("Successfully booked seat for user: {}", user.getEmail());
//						} else {
//							log.error("Failed to book seat: {}", response.getMessage());
//						}
//					});
//		} else {
//			log.error("User not found");
//		}
	}
}
