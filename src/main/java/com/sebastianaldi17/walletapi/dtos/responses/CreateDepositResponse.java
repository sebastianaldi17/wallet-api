package com.sebastianaldi17.walletapi.dtos.responses;

import com.sebastianaldi17.walletapi.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CreateDepositResponse {
    private BigDecimal amount;
    private UUID transactionId;
    private String idempotencyKey;
    private TransactionType type;
    private String description;
    private OffsetDateTime createdAt;
}
