package com.trainticketsystem.controller;

import com.trainticketsystem.response.TicketResponse;
import com.trainticketsystem.service.ModifySeatService;
import com.trainticketsystem.service.PurchaseTicketService;
import com.trainticketsystem.model.*;
import com.trainticketsystem.request.TicketRequest;
import com.trainticketsystem.service.ReceiptService;
import com.trainticketsystem.service.UserDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/train")
@RequiredArgsConstructor
@Slf4j
public class TrainController {

    private final PurchaseTicketService purchaseTicketService;
    private final ReceiptService receiptService;
    private final UserDetailService userInfoService;
    private final ModifySeatService modifySeatService;
    private final Map<Integer, User> userMap;
    private final Map<String, Seat> seatMap;

    @PostMapping("/purchase")
    public CompletableFuture<ResponseEntity<TicketResponse>> purchaseTicket(
            @RequestBody TicketRequest request) {
        return purchaseTicketService.purchaseTicket(request.getUserId(), request.getSeatId())
                .thenApply(response -> {
                    if (response.isTransactionStatus()) {
                        return ResponseEntity.ok(response);
                    }
                    return ResponseEntity.badRequest().body(response);
                });
    }

    @GetMapping("/receipt/{userId}")
    public ResponseEntity<TicketResponse> getReceiptDetails(@PathVariable String userId) {
        log.info("Fetching receipt for userId: {}", userId);
        return ResponseEntity.ok(receiptService.generateReceipt(userId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<TicketResponse> getUserDetails(@PathVariable String userId) {
        return ResponseEntity.ok(userInfoService.getUserDetails(userId));
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> removeUser(@PathVariable String userId) {
        userInfoService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/user/{userId}/seat")
    public CompletableFuture<ResponseEntity<TicketResponse>> modifyUserSeat(
            @RequestBody TicketRequest request) {
        return modifySeatService.modifySeating(request.getUserId(), request.getSeatId())
                .thenApply(response -> {
                    if (response.isTransactionStatus()) {
                        return ResponseEntity.ok(response);
                    }
                    return ResponseEntity.badRequest().body(response);
                });
    }

    @GetMapping("/user")
    public ResponseEntity<List<User>> getAllUsers() {
        LinkedHashMap<Integer, User> userList = new LinkedHashMap<>(userMap);
        return ResponseEntity.ok(userList.entrySet().stream().map(Map.Entry::getValue).toList());
    }

    @GetMapping("/seat")
    public ResponseEntity<List<Seat>> getAllSeat() {
        LinkedHashMap<String, Seat> userList = new LinkedHashMap<>(seatMap);
        return ResponseEntity.ok(userList.entrySet().stream().map(Map.Entry::getValue).toList());
    }

} 