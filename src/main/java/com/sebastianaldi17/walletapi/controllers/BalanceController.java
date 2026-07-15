package com.sebastianaldi17.walletapi.controllers;

import com.sebastianaldi17.walletapi.dtos.ErrorDto;
import com.sebastianaldi17.walletapi.dtos.GetBalanceDto;
import com.sebastianaldi17.walletapi.models.Balance;
import com.sebastianaldi17.walletapi.services.BalanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class BalanceController {
    private final BalanceService balanceService;

    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @GetMapping(path = "/balances")
    public ResponseEntity<Object> getBalance(
            @RequestHeader("X-API-KEY") String apiKey
    ) {
        try {
            Optional<Balance> balance = balanceService.getBalanceByApiKey(apiKey);
            return ResponseEntity.ok(new GetBalanceDto(balance.get().getAvailable(), balance.get().getLocked()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorDto(e.getMessage()));
        }
    }
}
