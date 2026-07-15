package com.sebastianaldi17.walletapi.repositories;

import com.sebastianaldi17.walletapi.models.Ledger;
import com.sebastianaldi17.walletapi.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LedgerRepository extends JpaRepository<Ledger, UUID> {
}
