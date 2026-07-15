package com.sebastianaldi17.walletapi.repositories;

import com.sebastianaldi17.walletapi.models.Account;
import com.sebastianaldi17.walletapi.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findOneByOwnerUserId(UUID ownerUserId);
}
