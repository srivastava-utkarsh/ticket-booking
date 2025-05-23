package com.trainticketsystem.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class AppProperties1 {
	private int userCount;
	private int seatCount;
	private int ticketPrice;
}
