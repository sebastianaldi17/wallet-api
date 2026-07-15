package com.sebastianaldi17.walletapi.services;

import com.sebastianaldi17.walletapi.models.Account;
import com.sebastianaldi17.walletapi.models.Balance;
import com.sebastianaldi17.walletapi.models.UserApiKey;
import com.sebastianaldi17.walletapi.repositories.AccountRepository;
import com.sebastianaldi17.walletapi.repositories.BalanceRepository;
import com.sebastianaldi17.walletapi.repositories.UserApiKeyRepository;
import com.sebastianaldi17.walletapi.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.Optional;

@Service
public class BalanceService {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private UserApiKeyRepository userApiKeyRepository;

    public Optional<Balance> getBalanceByApiKey(String apiKey) throws Exception {
        Optional<UserApiKey> userApiKey = userApiKeyRepository.findOneByApiKey(apiKey);
        if (userApiKey.isEmpty()) {
            throw new Exception("user not found");
        }
        Optional<Account> account = accountRepository.findOneByOwnerUserId(userApiKey.get().getUserId());
        if (account.isEmpty()) {
            throw new Exception("account not found");
        }
        Optional<Balance> balance = balanceRepository.findOneByAccountId(account.get().getId());
        if (balance.isEmpty()) {
            throw new Exception("balance not found");
        }

        return balance;
    }
}
