package com.sebastianaldi17.walletapi.dtos.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateTransferRequest {
    @NotNull
    @Positive
    BigDecimal amount;

    String description;

    @NotNull
    UUID recipientAccountId;
}
