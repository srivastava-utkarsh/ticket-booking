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

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AppConfig {

	private final AtomicInteger userIdGenerator = new AtomicInteger(1);
	private final AppProperties appProperties;

	@Bean
	public Map<String, Seat> seatMap() {
		Map<String, Seat> seats = new ConcurrentHashMap<>();
		IntStream.range(1, appProperties.getSeatCount()+1).forEach(i -> {
			String sA = "A" + i;
			String sB = "B" + i;
			seats.put(sA, new Seat(sA, true));
			seats.put(sB, new Seat(sB, true));
		});
		return seats;
	}

	@Bean
	public Map<Integer, User> userMap() {
		Map<Integer, User> users = new ConcurrentHashMap<>();
		IntStream.range(1, appProperties.getUserCount()+1).forEach(i -> {
			User user = User.builder()
					.id(userIdGenerator.getAndIncrement())
					.firstName("User_" + i)
					.lastName("last_name" + i)
					.email("user_" + i + "@test.com")
					.walletBalance(40)
					.build();
			users.put(user.getId(), user);
			log.info("Created new user with email : {} , id {}", user.getEmail(),user.getId());
		});
		return users;
	}
}
