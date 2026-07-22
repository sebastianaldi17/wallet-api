package com.sebastianaldi17.walletapi.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity(name = "ledger")
@Data
@DynamicInsert
public class Ledger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID transactionId;

    private UUID accountId;

    private String description;

    @Column(precision = 20, scale = 8)
    @ColumnDefault("0")
    private BigDecimal credit;

    @Column(precision = 20, scale = 8)
    @ColumnDefault("0")
    private BigDecimal debit;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @CreationTimestamp
    private OffsetDateTime createdAt;
}
