package com.sebastianaldi17.walletapi.models;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity(name = "ledger")
@Data
public class Ledger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID transactionId;

    private UUID accountId;

    @Column(precision = 20, scale = 8)
    private BigDecimal credit;

    @Column(precision = 20, scale = 8)
    private BigDecimal debit;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;
}
