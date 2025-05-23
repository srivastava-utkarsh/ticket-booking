package com.trainticketsystem.config;

import com.trainticketsystem.model.Seat;
import com.trainticketsystem.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Configuration class for initializing application beans and data structures.
 * Sets up initial users and seats with thread-safe collections.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class AppConfig {

	private final AtomicInteger userIdGenerator = new AtomicInteger(1);
	private final AppProperties appProperties;

	/**
	 * Creates and initializes the seat map with available seats.
	 * Creates seats for sections A and B based on configured seat count.
	 * 
	 * @return Map of seat numbers to Seat objects
	 */
	@Bean
	public Map<String, Seat> seatMap() {
		Map<String, Seat> seats = new ConcurrentHashMap<>();
		IntStream.range(1, appProperties.getSeatCount()+1).forEach(i -> {
			String sA = "A" + i;  // Section A seats
			String sB = "B" + i;  // Section B seats
			seats.put(sA, new Seat(sA, true));  // true means available
			seats.put(sB, new Seat(sB, true));
		});
		return seats;
	}

	/**
	 * Creates and initializes the user map with pre-configured users.
	 * Creates users based on configured user count with initial balance.
	 * 
	 * @return Map of user IDs to User objects
	 */
	@Bean
	public Map<Integer, User> userMap() {
		Map<Integer, User> users = new ConcurrentHashMap<>();
		IntStream.range(1, appProperties.getUserCount()+1).forEach(i -> {
			User user = User.builder()
					.id(userIdGenerator.getAndIncrement())
					.firstName("User_" + i)
					.lastName("last_name" + i)
					.email("user_" + i + "@test.com")
					.walletBalance(appProperties.getWalletBalance())
					.build();
			users.put(user.getId(), user);
			log.info("Created new user with email : {} , id {}", user.getEmail(),user.getId());
		});
		return users;
	}
}
