package com.sebastianaldi17.walletapi.services;

import com.sebastianaldi17.walletapi.exceptions.ResourceNotFoundException;
import com.sebastianaldi17.walletapi.models.Account;
import com.sebastianaldi17.walletapi.models.Balance;
import com.sebastianaldi17.walletapi.repositories.AccountRepository;
import com.sebastianaldi17.walletapi.repositories.BalanceRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BalanceService {
    private final AccountRepository accountRepository;

    private final BalanceRepository balanceRepository;

    public BalanceService(AccountRepository accountRepository, BalanceRepository balanceRepository) {
        this.accountRepository = accountRepository;
        this.balanceRepository = balanceRepository;
    }

    public Balance getBalanceByUserId(UUID userId) throws RuntimeException {
        Account account = accountRepository.findOneByOwnerUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("account not found"));
        return balanceRepository.findOneByAccountId(account.getId())
                .orElseThrow(() -> new ResourceNotFoundException("balance not found"));

    }
}
