package com.sebastianaldi17.walletapi.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CreateDepositDto {
    private BigDecimal amount;
    private UUID userId;
    private String idempotencyKey;
    private String description;
}
