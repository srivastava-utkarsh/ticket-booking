package com.trainticketsystem.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.concurrent.locks.ReentrantLock;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Seat {
	private final String id;
	private boolean isAvailable;
	private User reservedBy;

	@JsonIgnore
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
