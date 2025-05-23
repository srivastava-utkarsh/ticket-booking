package com.trainticketsystem.utils;

import com.trainticketsystem.config.AppProperties;
import com.trainticketsystem.model.BookingResult;
import com.trainticketsystem.model.Ticket;
import com.trainticketsystem.model.User;
import com.trainticketsystem.response.TicketResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class TicketResponseBuilder {
	private final AtomicLong ticketIdGenerator = new AtomicLong(1);
	private final AppProperties appProperties;

	public TicketResponseBuilder(AppProperties appProperties) {
		this.appProperties = appProperties;
	}

	public TicketResponse sendPurchaseSuccessResponse(User user, String seatNumber) {
		Ticket ticket = Ticket.builder()
								.id(ticketIdGenerator.getAndIncrement())
								.fromLocation("London")
								.toLocation("France")
								.price(appProperties.getTicketPrice())
								.seatNumber(seatNumber)
								.section(seatNumber.startsWith("A") ? "A" : "B")
								.userEmail(user.getEmail())
								.userFirstName(user.getFirstName())
								.userLastName(user.getLastName())
								.build();
		user.setTicket(ticket);
		log.info("Ticket created successfully for user {} with seat {}", user.getEmail(), seatNumber);

		return TicketResponse.builder()
								.transactionStatus(true)
								.message("Ticket purchased successfully")
								.ticket(ticket)
								.build();
	}

	public TicketResponse sendFailedResponse(BookingResult bookingResult , String errMessage) {
		String message = StringUtils.hasText(errMessage) ? errMessage : bookingResult.getMessage();
		return TicketResponse.builder()
								.transactionStatus(false)
								.message(message)
								.build();
	}

	public TicketResponse sendTicketResponse(Ticket ticket) {
		return TicketResponse.builder()
							.transactionStatus(true)
							.ticket(ticket)
							.build();
	}

	public TicketResponse sendUserResponse(User user) {
		return TicketResponse.builder()
				.transactionStatus(true)
				.user(user)
				.build();
	}
}
