package com.sebastianaldi17.walletapi.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class GetBalanceResponse {
    private BigDecimal available;
    private BigDecimal locked;
}
