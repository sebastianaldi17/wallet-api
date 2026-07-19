package com.sebastianaldi17.walletapi.models;

import com.sebastianaldi17.walletapi.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity(name = "transactions")
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID accountId;

    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", columnDefinition = "transaction_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private TransactionType type;

    private String description;

    @Column(precision = 20, scale = 8)
    private BigDecimal amount;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE", nullable = false)
    @CreationTimestamp
    private OffsetDateTime createdAt;
}
