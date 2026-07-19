package com.sebastianaldi17.walletapi.repositories;

import com.sebastianaldi17.walletapi.models.Balance;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BalanceRepository extends JpaRepository<Balance, Long> {
    Optional<Balance> findOneByAccountId(UUID accountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Balance> findOneForUpdateByAccountId(UUID accountId);
}
