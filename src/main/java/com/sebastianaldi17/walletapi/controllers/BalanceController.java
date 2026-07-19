package com.sebastianaldi17.walletapi.controllers;

import com.sebastianaldi17.walletapi.dtos.responses.GetBalanceResponse;
import com.sebastianaldi17.walletapi.models.Balance;
import com.sebastianaldi17.walletapi.services.BalanceService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class BalanceController {
    private final BalanceService balanceService;

    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @GetMapping(path = "/balances")
    public GetBalanceResponse getBalance() {
        UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Balance balance = balanceService.getBalanceByUserId(userId);
        return new GetBalanceResponse(balance.getAvailable(), balance.getLocked());

    }
}
