package com.sebastianaldi17.walletapi.dtos.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateDepositRequest {
    @NotNull
    @Positive
    BigDecimal amount;

    String description;
}
