package com.trainticketsystem.request;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TicketRequest {
	@NotNull
	private String userId;

	@NotNull
	private String seatId;

	private String from;
	private String to;

	private TicketRequest(){
		this.from = "London";
		this.to = "France";
	}
}
