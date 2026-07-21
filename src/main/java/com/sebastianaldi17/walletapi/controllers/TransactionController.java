package com.sebastianaldi17.walletapi.controllers;

import com.sebastianaldi17.walletapi.dtos.CreateDepositDto;
import com.sebastianaldi17.walletapi.dtos.CreateTransferDto;
import com.sebastianaldi17.walletapi.dtos.CreateWithdrawalDto;
import com.sebastianaldi17.walletapi.dtos.requests.CreateDepositRequest;
import com.sebastianaldi17.walletapi.dtos.requests.CreateTransferRequest;
import com.sebastianaldi17.walletapi.dtos.requests.CreateWithdrawalRequest;
import com.sebastianaldi17.walletapi.dtos.responses.CreateDepositResponse;
import com.sebastianaldi17.walletapi.dtos.responses.CreateTransferResponse;
import com.sebastianaldi17.walletapi.dtos.responses.CreateWithdrawalResponse;
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

    @PostMapping(path = "/transactions/_withdraw")
    public CreateWithdrawalResponse createWithdrawal(
            @RequestHeader(name = "Idempotency-Key")
            String idempotencyKey,
            @RequestBody @Valid
            CreateWithdrawalRequest request
    ) {
        UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CreateWithdrawalDto dto = new CreateWithdrawalDto(request.getAmount(), userId, idempotencyKey, request.getDescription());
        return transactionService.createWithdrawal(dto);
    }

    @PostMapping(path = "/transactions/_transfer")
    public CreateTransferResponse createTransfer(
            @RequestHeader(name = "Idempotency-Key")
            String idempotencyKey,
            @RequestBody @Valid
            CreateTransferRequest request
    ) {
        UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CreateTransferDto dto = new CreateTransferDto(request.getAmount(), userId, request.getRecipientAccountId(), idempotencyKey, request.getDescription());
        return transactionService.createTransfer(dto);
    }
}
