package com.sebastianaldi17.walletapi.dtos.responses;

import com.sebastianaldi17.walletapi.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class CreateWithdrawalResponse {
    private BigDecimal amount;
    private String idempotencyKey;
    private TransactionType type;
    private String description;
    private OffsetDateTime createdAt;
}
