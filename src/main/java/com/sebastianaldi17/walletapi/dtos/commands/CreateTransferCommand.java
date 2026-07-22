package com.sebastianaldi17.walletapi.dtos.commands;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CreateTransferCommand {
    private BigDecimal amount;
    private UUID userId;
    private UUID recipientAccountId;
    private String idempotencyKey;
    private String description;
}
