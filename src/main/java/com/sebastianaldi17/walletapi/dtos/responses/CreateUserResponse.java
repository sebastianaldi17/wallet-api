package com.sebastianaldi17.walletapi.dtos.responses;

import com.sebastianaldi17.walletapi.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CreateUserResponse {
    private UUID id;
    private UUID accountId;
    private String description;
    private String apiKey;
}
