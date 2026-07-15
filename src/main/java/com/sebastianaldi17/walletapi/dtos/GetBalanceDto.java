package com.sebastianaldi17.walletapi.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class GetBalanceDto {
    private BigDecimal available;
    private BigDecimal locked;
}
