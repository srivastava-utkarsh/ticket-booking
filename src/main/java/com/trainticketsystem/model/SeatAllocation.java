package com.trainticketsystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatAllocation {
    private String seatNumber;
    private String userEmail;
    private boolean isAvailable;
} 