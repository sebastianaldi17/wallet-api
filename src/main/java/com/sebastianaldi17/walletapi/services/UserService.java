package com.sebastianaldi17.walletapi.services;

import com.sebastianaldi17.walletapi.components.ApiKeyHasher;
import com.sebastianaldi17.walletapi.dtos.requests.CreateUserRequest;
import com.sebastianaldi17.walletapi.dtos.responses.CreateUserResponse;
import com.sebastianaldi17.walletapi.enums.AccountType;
import com.sebastianaldi17.walletapi.enums.UserRole;
import com.sebastianaldi17.walletapi.models.Account;
import com.sebastianaldi17.walletapi.models.Balance;
import com.sebastianaldi17.walletapi.models.User;
import com.sebastianaldi17.walletapi.models.UserApiKey;
import com.sebastianaldi17.walletapi.repositories.AccountRepository;
import com.sebastianaldi17.walletapi.repositories.BalanceRepository;
import com.sebastianaldi17.walletapi.repositories.UserApiKeyRepository;
import com.sebastianaldi17.walletapi.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {
    private final AccountRepository accountRepository;
    private final BalanceRepository balanceRepository;
    private final UserRepository userRepository;
    private final UserApiKeyRepository userApiKeyRepository;


    public UserService(UserRepository userRepository, UserApiKeyRepository userApiKeyRepository, AccountRepository accountRepository, BalanceRepository balanceRepository) {
        this.accountRepository = accountRepository;
        this.balanceRepository = balanceRepository;
        this.userRepository = userRepository;
        this.userApiKeyRepository = userApiKeyRepository;
    }

    @Transactional
    public CreateUserResponse createUser(CreateUserRequest request) throws RuntimeException {
        User newUser = new User();
        newUser.setDescription(request.getDescription());
        newUser.setRole(UserRole.USER);
        userRepository.save(newUser);

        UUID apiKey = UUID.randomUUID();
        UserApiKey userApiKey = new UserApiKey();
        userApiKey.setApiKey(ApiKeyHasher.hash(apiKey.toString()));
        userApiKey.setUserId(newUser.getId());
        userApiKeyRepository.save(userApiKey);

        Account account = new Account();
        account.setOwnerUserId(newUser.getId());
        account.setAccountType(AccountType.USER);
        accountRepository.save(account);

        Balance balance = new Balance();
        balance.setAccountId(account.getId());
        balanceRepository.save(balance);

        return new CreateUserResponse(newUser.getId(), account.getId(), request.getDescription(), apiKey.toString());
    }
}
