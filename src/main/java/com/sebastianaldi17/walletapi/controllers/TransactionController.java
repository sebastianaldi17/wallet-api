package com.sebastianaldi17.walletapi.controllers;

import com.sebastianaldi17.walletapi.dtos.commands.CreateDepositCommand;
import com.sebastianaldi17.walletapi.dtos.commands.CreateTransferCommand;
import com.sebastianaldi17.walletapi.dtos.commands.CreateWithdrawalCommand;
import com.sebastianaldi17.walletapi.dtos.requests.CreateDepositRequest;
import com.sebastianaldi17.walletapi.dtos.requests.CreateTransferRequest;
import com.sebastianaldi17.walletapi.dtos.requests.CreateWithdrawalRequest;
import com.sebastianaldi17.walletapi.dtos.responses.CreateDepositResponse;
import com.sebastianaldi17.walletapi.dtos.responses.CreateTransferResponse;
import com.sebastianaldi17.walletapi.dtos.responses.CreateWithdrawalResponse;
import com.sebastianaldi17.walletapi.models.Transaction;
import com.sebastianaldi17.walletapi.services.TransactionService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
        CreateDepositCommand dto = new CreateDepositCommand(request.getAmount(), userId, idempotencyKey, request.getDescription());
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
        CreateWithdrawalCommand dto = new CreateWithdrawalCommand(request.getAmount(), userId, idempotencyKey, request.getDescription());
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
        CreateTransferCommand dto = new CreateTransferCommand(request.getAmount(), userId, request.getRecipientAccountId(), idempotencyKey, request.getDescription());
        return transactionService.createTransfer(dto);
    }

    @GetMapping(path = "/transactions/{transactionId}")
    public Transaction getTransactionById(
            @PathVariable("transactionId") UUID transactionId
    ) {
        UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return transactionService.getTransactionById(userId, transactionId);
    }

    @GetMapping(path = "/transactions")
    public List<Transaction> getTransactions(
            @PageableDefault(page = 0, size = 10, sort = "createdAt") @ParameterObject
            Pageable pageable
    ) {
        UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return transactionService.getTransactionsByUserId(userId, pageable);
    }
}
