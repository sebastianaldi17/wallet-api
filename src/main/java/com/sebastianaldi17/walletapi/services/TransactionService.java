package com.sebastianaldi17.walletapi.services;

import com.sebastianaldi17.walletapi.dtos.CreateDepositDto;
import com.sebastianaldi17.walletapi.dtos.CreateTransferDto;
import com.sebastianaldi17.walletapi.dtos.CreateWithdrawalDto;
import com.sebastianaldi17.walletapi.dtos.responses.CreateDepositResponse;
import com.sebastianaldi17.walletapi.dtos.responses.CreateTransferResponse;
import com.sebastianaldi17.walletapi.dtos.responses.CreateWithdrawalResponse;
import com.sebastianaldi17.walletapi.enums.TransactionType;
import com.sebastianaldi17.walletapi.exceptions.IdempotencyKeyReuseException;
import com.sebastianaldi17.walletapi.exceptions.InsufficientBalanceException;
import com.sebastianaldi17.walletapi.exceptions.ResourceNotFoundException;
import com.sebastianaldi17.walletapi.models.Account;
import com.sebastianaldi17.walletapi.models.Balance;
import com.sebastianaldi17.walletapi.models.Ledger;
import com.sebastianaldi17.walletapi.models.Transaction;
import com.sebastianaldi17.walletapi.repositories.AccountRepository;
import com.sebastianaldi17.walletapi.repositories.BalanceRepository;
import com.sebastianaldi17.walletapi.repositories.LedgerRepository;
import com.sebastianaldi17.walletapi.repositories.TransactionRepository;
import org.springframework.transaction.annotation.Transactional;
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

    private final UUID systemClearingAccount = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Transactional
    public CreateDepositResponse createDeposit(CreateDepositDto dto) throws RuntimeException {
        Account account = accountRepository.findOneByOwnerUserId(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("account not found"));

        Optional<Transaction> optionalTransaction = transactionRepository.findOneByAccountIdAndIdempotencyKey(account.getId(), dto.getIdempotencyKey());
        if(optionalTransaction.isPresent()) {
            throw new IdempotencyKeyReuseException("idempotency key already used");
        }

        Balance balance = balanceRepository.findOneForUpdateByAccountId(account.getId())
                .orElseThrow(() -> new ResourceNotFoundException("balance not found"));

        balance.setAvailable(balance.getAvailable().add(dto.getAmount()));
        balanceRepository.save(balance); // apparently not needed since @Transactional automatically save at the end, but just for clarity

        Transaction newTransaction = new Transaction();
        newTransaction.setAccountId(account.getId());
        newTransaction.setAmount(dto.getAmount());
        newTransaction.setIdempotencyKey(dto.getIdempotencyKey());
        newTransaction.setType(TransactionType.DEPOSIT);
        newTransaction.setDescription(dto.getDescription());
        transactionRepository.save(newTransaction);

        Ledger creditLedger = new Ledger();
        Ledger debitLedger = new Ledger();

        creditLedger.setTransactionId(newTransaction.getId());
        debitLedger.setTransactionId(newTransaction.getId());

        creditLedger.setAccountId(account.getId());
        debitLedger.setAccountId(systemClearingAccount);

        creditLedger.setCredit(dto.getAmount());
        debitLedger.setDebit(dto.getAmount());

        String description = String.format("Deposit for account ID %s", account.getId());
        creditLedger.setDescription(description);
        debitLedger.setDescription(description);
        ledgerRepository.save(creditLedger);
        ledgerRepository.save(debitLedger);

        return new CreateDepositResponse(newTransaction.getAmount(), newTransaction.getIdempotencyKey(), newTransaction.getType(), newTransaction.getDescription(), newTransaction.getCreatedAt());
    }

    @Transactional
    public CreateWithdrawalResponse createWithdrawal(CreateWithdrawalDto dto) throws RuntimeException {
        Account account = accountRepository.findOneByOwnerUserId(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("account not found"));

        Optional<Transaction> optionalTransaction = transactionRepository.findOneByAccountIdAndIdempotencyKey(account.getId(), dto.getIdempotencyKey());
        if(optionalTransaction.isPresent()) {
            throw new IdempotencyKeyReuseException("idempotency key already used");
        }

        Balance balance = balanceRepository.findOneForUpdateByAccountId(account.getId())
                .orElseThrow(() -> new ResourceNotFoundException("balance not found"));

        if(balance.getAvailable().compareTo(dto.getAmount()) < 0) {
            throw new InsufficientBalanceException("balance is not enough to do withdrawal");
        }

        balance.setAvailable(balance.getAvailable().subtract(dto.getAmount()));
        balanceRepository.save(balance);

        Transaction newTransaction = new Transaction();
        newTransaction.setAccountId(account.getId());
        newTransaction.setAmount(dto.getAmount());
        newTransaction.setIdempotencyKey(dto.getIdempotencyKey());
        newTransaction.setType(TransactionType.WITHDRAWAL);
        newTransaction.setDescription(dto.getDescription());
        transactionRepository.save(newTransaction);

        Ledger creditLedger = new Ledger();
        Ledger debitLedger = new Ledger();

        creditLedger.setTransactionId(newTransaction.getId());
        debitLedger.setTransactionId(newTransaction.getId());

        creditLedger.setAccountId(account.getId());
        debitLedger.setAccountId(systemClearingAccount);

        creditLedger.setCredit(dto.getAmount());
        debitLedger.setDebit(dto.getAmount());

        String description = String.format("Withdrawal for account ID %s", account.getId());
        creditLedger.setDescription(description);
        debitLedger.setDescription(description);
        ledgerRepository.save(creditLedger);
        ledgerRepository.save(debitLedger);

        return new CreateWithdrawalResponse(newTransaction.getAmount(), newTransaction.getIdempotencyKey(), newTransaction.getType(), newTransaction.getDescription(), newTransaction.getCreatedAt());
    }

    @Transactional
    public CreateTransferResponse createTransfer(CreateTransferDto dto) throws RuntimeException {
        Account account = accountRepository.findOneByOwnerUserId(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("account not found"));

        Optional<Transaction> optionalTransaction = transactionRepository.findOneByAccountIdAndIdempotencyKey(account.getId(), dto.getIdempotencyKey());
        if(optionalTransaction.isPresent()) {
            throw new IdempotencyKeyReuseException("idempotency key already used");
        }

        Balance balance = balanceRepository.findOneForUpdateByAccountId(account.getId())
                .orElseThrow(() -> new ResourceNotFoundException("balance not found"));

        if(balance.getAvailable().compareTo(dto.getAmount()) < 0) {
            throw new InsufficientBalanceException("balance is not enough to do transfer");
        }

        Balance recipientBalance = balanceRepository.findOneForUpdateByAccountId(dto.getRecipientAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("recipient not found"));

        balance.setAvailable(balance.getAvailable().subtract(dto.getAmount()));
        recipientBalance.setAvailable(recipientBalance.getAvailable().add(dto.getAmount()));
        balanceRepository.save(balance);
        balanceRepository.save(recipientBalance);

        Transaction newTransaction = new Transaction();
        newTransaction.setAccountId(account.getId());
        newTransaction.setAmount(dto.getAmount());
        newTransaction.setIdempotencyKey(dto.getIdempotencyKey());
        newTransaction.setType(TransactionType.TRANSFER);
        newTransaction.setDescription(dto.getDescription());
        transactionRepository.save(newTransaction);

        Ledger creditLedger = new Ledger();
        Ledger debitLedger = new Ledger();

        creditLedger.setTransactionId(newTransaction.getId());
        debitLedger.setTransactionId(newTransaction.getId());

        creditLedger.setAccountId(account.getId());
        debitLedger.setAccountId(dto.getRecipientAccountId());

        creditLedger.setCredit(dto.getAmount());
        debitLedger.setDebit(dto.getAmount());

        String description = String.format("Transfer from account ID %s to account ID %s", account.getId(), dto.getRecipientAccountId());
        creditLedger.setDescription(description);
        debitLedger.setDescription(description);
        ledgerRepository.save(creditLedger);
        ledgerRepository.save(debitLedger);

        return new CreateTransferResponse(newTransaction.getAmount(), dto.getRecipientAccountId(), newTransaction.getIdempotencyKey(), newTransaction.getType(), newTransaction.getDescription(), newTransaction.getCreatedAt());
    }
}
