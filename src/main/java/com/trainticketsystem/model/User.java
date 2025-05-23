package com.trainticketsystem.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {
	@JsonIgnore
	private Integer id;
	private String firstName;
	private String lastName;
	private String email;
	
	@JsonIgnore
	private int walletBalance;
	
	@JsonIgnore
	private Ticket ticket;

	private String seatNumber;

	public void setTicket(Ticket ticket) {
		this.ticket = ticket;
		this.seatNumber = ticket != null ? ticket.getSeatNumber() : null;
	}

	public boolean deductBalance(int amount) {
		if (walletBalance >= amount) {
			walletBalance -= amount;
			return true;
		}
		return false;
	}

	public void addBalance(int amount) {
		walletBalance += amount;
	}
}
