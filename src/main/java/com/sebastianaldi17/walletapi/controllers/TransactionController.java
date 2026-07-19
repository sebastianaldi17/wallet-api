package com.sebastianaldi17.walletapi.controllers;

import com.sebastianaldi17.walletapi.dtos.CreateDepositDto;
import com.sebastianaldi17.walletapi.dtos.requests.CreateDepositRequest;
import com.sebastianaldi17.walletapi.dtos.responses.CreateDepositResponse;
import com.sebastianaldi17.walletapi.services.TransactionService;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping(path = "/transactions/_deposit")
    public CreateDepositResponse createDeposit(
            @RequestHeader(name = "Idempotency-Key")
            String idempotencyKey,
            @RequestBody @Valid
            CreateDepositRequest request
    ) {
        UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CreateDepositDto dto = new CreateDepositDto(request.getAmount(), userId, idempotencyKey, request.getDescription());
        return transactionService.createDeposit(dto);
    }
}
