package com.sebastianaldi17.walletapi.services;

import com.sebastianaldi17.walletapi.dtos.CreateDepositDto;
import com.sebastianaldi17.walletapi.dtos.responses.CreateDepositResponse;
import com.sebastianaldi17.walletapi.enums.TransactionType;
import com.sebastianaldi17.walletapi.exceptions.ResourceNotFoundException;
import com.sebastianaldi17.walletapi.models.Account;
import com.sebastianaldi17.walletapi.models.Balance;
import com.sebastianaldi17.walletapi.models.Ledger;
import com.sebastianaldi17.walletapi.models.Transaction;
import com.sebastianaldi17.walletapi.repositories.AccountRepository;
import com.sebastianaldi17.walletapi.repositories.BalanceRepository;
import com.sebastianaldi17.walletapi.repositories.LedgerRepository;
import com.sebastianaldi17.walletapi.repositories.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionService {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private LedgerRepository ledgerRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private final UUID SystemClearingAccount = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Transactional
    public CreateDepositResponse createDeposit(CreateDepositDto dto) throws RuntimeException {
        Account account = accountRepository.findOneByOwnerUserId(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("account not found"));

        Optional<Transaction> optionalTransaction = transactionRepository.findOneForUpdateByAccountIdAndIdempotencyKey(account.getId(), dto.getIdempotencyKey());
        if(optionalTransaction.isPresent()) {
            Transaction transaction = optionalTransaction.get();
            return new CreateDepositResponse(transaction.getAmount(), transaction.getIdempotencyKey(), transaction.getType(), transaction.getDescription(), transaction.getCreatedAt());
        }

        Balance balance = balanceRepository.findOneForUpdateByAccountId(account.getId())
                .orElseThrow(() -> new ResourceNotFoundException("balance not found"));

        balance.setAvailable(balance.getAvailable().add(dto.getAmount()));
        balanceRepository.save(balance);

        Transaction newTransaction = new Transaction();
        newTransaction.setAccountId(account.getId());
        newTransaction.setAmount(dto.getAmount());
        newTransaction.setIdempotencyKey(dto.getIdempotencyKey());
        newTransaction.setType(TransactionType.DEPOSIT);
        newTransaction.setDescription(dto.getDescription());
        Transaction savedTransaction = transactionRepository.save(newTransaction);

        Ledger creditLedger = new Ledger();
        Ledger debitLedger = new Ledger();

        creditLedger.setTransactionId(newTransaction.getId());
        debitLedger.setTransactionId(newTransaction.getId());

        creditLedger.setAccountId(SystemClearingAccount);
        debitLedger.setAccountId(account.getId());

        creditLedger.setCredit(dto.getAmount());
        debitLedger.setDebit(dto.getAmount());

        String description = String.format("Deposit for account ID %s", account.getId());
        creditLedger.setDescription(description);
        debitLedger.setDescription(description);
        ledgerRepository.save(creditLedger);
        ledgerRepository.save(debitLedger);

        return new CreateDepositResponse(newTransaction.getAmount(), newTransaction.getIdempotencyKey(), newTransaction.getType(), newTransaction.getDescription(), newTransaction.getCreatedAt());
    }
}
