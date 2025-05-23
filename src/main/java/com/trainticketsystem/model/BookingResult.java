package com.trainticketsystem.model;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class BookingResult {
	private final boolean success;
	private final String message;
	private final int totalPrice;
	private final List<String> bookedSeats;

	public BookingResult(boolean success, String message, int totalPrice, List<String> bookedSeats) {
		this.success = success;
		this.message = message;
		this.totalPrice = totalPrice;
		this.bookedSeats = bookedSeats;
	}

	// Factory methods
	public static BookingResult success() {
		return new BookingResult(true, "Booking successful", 0, Collections.emptyList());
	}

	public static BookingResult failed(String reason) {
		return new BookingResult(false, reason, 0, List.of());
	}

	public boolean isSuccess() {
		return success;
	}

	public List<String> getBookedSeats() {
		return bookedSeats;
	}

	@Override
	public String toString() {
		return "BookingResult{" +
				"success=" + success +
				", message='" + message + '\'' +
				", bookedSeats=" + bookedSeats +
				'}';
	}
}
