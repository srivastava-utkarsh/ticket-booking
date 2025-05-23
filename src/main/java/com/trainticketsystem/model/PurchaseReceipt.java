package com.trainticketsystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseReceipt {
    private String from;
    private String to;
    private User user;
    private int price;
    private Seat seat;
}
