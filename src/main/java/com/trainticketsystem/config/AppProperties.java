package com.trainticketsystem.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties class for train ticket system.
 * Maps properties from application.yml with prefix 'app'.
 * Used to configure system-wide settings like prices and counts.
 */
@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class AppProperties {
	/**
	 * Price of a single train ticket in dollars.
	 * Configured in application.yml as app.ticket-price.
	 */
	private int ticketPrice;

	/**
	 * Total number of users to be pre-created in the system.
	 * Configured in application.yml as app.user-count.
	 */
	private int userCount;

	/**
	 * Number of seats to be created per section (A and B).
	 * Total seats will be seatCount * 2 (for both sections).
	 * Configured in application.yml as app.seat-count.
	 */
	private int seatCount;

	/**
	 * Initial wallet balance for each user in dollars.
	 * Configured in application.yml as app.wallet-balance.
	 * Used when creating new users in the system.
	 */
	private int walletBalance;
}
