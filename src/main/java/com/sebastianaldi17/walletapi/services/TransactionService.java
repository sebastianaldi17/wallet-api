package com.sebastianaldi17.walletapi.services;

import com.sebastianaldi17.walletapi.dtos.commands.CreateDepositCommand;
import com.sebastianaldi17.walletapi.dtos.commands.CreateTransferCommand;
import com.sebastianaldi17.walletapi.dtos.commands.CreateWithdrawalCommand;
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
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionService {
    private final AccountRepository accountRepository;
    private final BalanceRepository balanceRepository;
    private final LedgerRepository ledgerRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(AccountRepository accountRepository, BalanceRepository balanceRepository, LedgerRepository ledgerRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.balanceRepository = balanceRepository;
        this.ledgerRepository = ledgerRepository;
        this.transactionRepository = transactionRepository;
    }

    private final UUID systemClearingAccount = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Transactional
    public CreateDepositResponse createDeposit(CreateDepositCommand dto) throws RuntimeException {
        Account account = accountRepository.findOneByOwnerUserId(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("account not found"));

        Optional<Transaction> optionalTransaction = transactionRepository.findOneByAccountIdAndIdempotencyKey(account.getId(), dto.getIdempotencyKey());
        if(optionalTransaction.isPresent()) {
            Transaction transaction = optionalTransaction.get();
            if(!transaction.getAmount().equals(dto.getAmount()) || !transaction.getDescription().equals(dto.getDescription()) || !transaction.getType().equals(TransactionType.DEPOSIT)) {
                throw new IdempotencyKeyReuseException("idempotency key already used");
            }
            return new CreateDepositResponse(transaction.getAmount(), transaction.getId(), transaction.getIdempotencyKey(), transaction.getType(), transaction.getDescription(), transaction.getCreatedAt());
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

        return new CreateDepositResponse(newTransaction.getAmount(), newTransaction.getId(), newTransaction.getIdempotencyKey(), newTransaction.getType(), newTransaction.getDescription(), newTransaction.getCreatedAt());
    }

    @Transactional
    public CreateWithdrawalResponse createWithdrawal(CreateWithdrawalCommand dto) throws RuntimeException {
        Account account = accountRepository.findOneByOwnerUserId(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("account not found"));

        Optional<Transaction> optionalTransaction = transactionRepository.findOneByAccountIdAndIdempotencyKey(account.getId(), dto.getIdempotencyKey());
        if(optionalTransaction.isPresent()) {
            Transaction transaction = optionalTransaction.get();
            if(!transaction.getAmount().equals(dto.getAmount()) || !transaction.getDescription().equals(dto.getDescription()) || !transaction.getType().equals(TransactionType.WITHDRAWAL)) {
                throw new IdempotencyKeyReuseException("idempotency key already used");
            }
            return new CreateWithdrawalResponse(transaction.getAmount(), transaction.getId(), transaction.getIdempotencyKey(), transaction.getType(), transaction.getDescription(), transaction.getCreatedAt());
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

        creditLedger.setAccountId(systemClearingAccount);
        debitLedger.setAccountId(account.getId());

        creditLedger.setCredit(dto.getAmount());
        debitLedger.setDebit(dto.getAmount());

        String description = String.format("Withdrawal for account ID %s", account.getId());
        creditLedger.setDescription(description);
        debitLedger.setDescription(description);
        ledgerRepository.save(creditLedger);
        ledgerRepository.save(debitLedger);

        return new CreateWithdrawalResponse(newTransaction.getAmount(), newTransaction.getId(), newTransaction.getIdempotencyKey(), newTransaction.getType(), newTransaction.getDescription(), newTransaction.getCreatedAt());
    }

    @Transactional
    public CreateTransferResponse createTransfer(CreateTransferCommand dto) throws RuntimeException {
        Account account = accountRepository.findOneByOwnerUserId(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("account not found"));

        Optional<Transaction> optionalTransaction = transactionRepository.findOneByAccountIdAndIdempotencyKey(account.getId(), dto.getIdempotencyKey());
        if(optionalTransaction.isPresent()) {
            Transaction transaction = optionalTransaction.get();
            if(!transaction.getAmount().equals(dto.getAmount()) || !transaction.getDescription().equals(dto.getDescription()) || !transaction.getType().equals(TransactionType.TRANSFER)) {
                throw new IdempotencyKeyReuseException("idempotency key already used");
            }

            // resolve destination account via ledger
            List<Ledger> ledgers = ledgerRepository.findAllByTransactionId(transaction.getId());
            for(Ledger ledger: ledgers) {
                if(!ledger.getAccountId().equals(account.getId()) && !ledger.getAccountId().equals(dto.getRecipientAccountId())) {
                    throw new IdempotencyKeyReuseException("idempotency key already used");
                }
            }
            return new CreateTransferResponse(transaction.getAmount(), transaction.getAccountId(), transaction.getId(), transaction.getIdempotencyKey(), transaction.getType(), transaction.getDescription(), transaction.getCreatedAt());
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

        creditLedger.setAccountId(dto.getRecipientAccountId());
        debitLedger.setAccountId(account.getId());

        creditLedger.setCredit(dto.getAmount());
        debitLedger.setDebit(dto.getAmount());

        String description = String.format("Transfer from account ID %s to account ID %s", account.getId(), dto.getRecipientAccountId());
        creditLedger.setDescription(description);
        debitLedger.setDescription(description);
        ledgerRepository.save(creditLedger);
        ledgerRepository.save(debitLedger);

        return new CreateTransferResponse(newTransaction.getAmount(), dto.getRecipientAccountId(), newTransaction.getId(), newTransaction.getIdempotencyKey(), newTransaction.getType(), newTransaction.getDescription(), newTransaction.getCreatedAt());
    }

    public Transaction getTransactionById(UUID userId, UUID transactionId) throws RuntimeException {
        Account account = accountRepository.findOneByOwnerUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("account not found"));

        return transactionRepository.findOneByAccountIdAndId(account.getId(), transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("transaction not found"));
    }

    public List<Transaction> getTransactionsByUserId(UUID userId, Pageable pageable) throws RuntimeException {
        Account account = accountRepository.findOneByOwnerUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("account not found"));

        return transactionRepository.findAllByAccountId(account.getId(), pageable);
    }
}
