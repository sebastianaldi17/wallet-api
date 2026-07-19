package com.sebastianaldi17.walletapi.repositories;

import com.sebastianaldi17.walletapi.models.Transaction;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Transaction> findOneForUpdateByAccountIdAndIdempotencyKey(UUID accountId, String idempotencyKey);
}
