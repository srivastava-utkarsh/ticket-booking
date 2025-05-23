package com.trainticketsystem.model;

import lombok.Data;

import java.util.concurrent.locks.ReentrantLock;

@Data
public class Seat {
	private final String id;
	private boolean isAvailable;
	private User reservedBy;
	private final ReentrantLock lock = new ReentrantLock();

	public Seat(String id, boolean isAvailable) {
		this.id = id;
		this.isAvailable = isAvailable;
	}

	public boolean reserve(User user){
		if (isAvailable){
			isAvailable = false;
			reservedBy = user;
			return true;
		}
		return false;
	}

	public boolean isAvailable(){
		return isAvailable;
	}
}
