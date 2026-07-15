package com.sebastianaldi17.walletapi.repositories;

import com.sebastianaldi17.walletapi.models.Account;
import com.sebastianaldi17.walletapi.models.Balance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BalanceRepository extends JpaRepository<Balance, Long> {
    Optional<Balance> findOneByAccountId(UUID accountId);
}
