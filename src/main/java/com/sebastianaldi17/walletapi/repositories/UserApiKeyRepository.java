package com.sebastianaldi17.walletapi.repositories;

import com.sebastianaldi17.walletapi.models.User;
import com.sebastianaldi17.walletapi.models.UserApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserApiKeyRepository extends JpaRepository<UserApiKey, Long> {
    Optional<UserApiKey> findOneByApiKey(String apiKey);
}
