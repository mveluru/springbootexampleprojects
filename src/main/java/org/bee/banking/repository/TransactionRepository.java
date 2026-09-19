package org.bee.banking.repository;

import lombok.extern.slf4j.Slf4j;
import org.bee.banking.domain.AccountTransaction;
import org.bee.banking.messages.BankingMessages;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Repository
public class TransactionRepository {
    private final Map<String, List<AccountTransaction>> transactionHistory = new ConcurrentHashMap<>();

    public void recordTransaction(AccountTransaction transaction) {
        Assert.notNull(transaction, "transaction must not be null");
        transactionHistory
                .computeIfAbsent(transaction.getAccountNumber(), key -> new CopyOnWriteArrayList<>())
                .add(transaction);
        log.info(BankingMessages.LOG_TRANSACTION_RECORDED,
                transaction.getTransactionType(), transaction.getAccountNumber(), transaction.getAmount(), transaction.getBalanceAfter());
    }

    public List<AccountTransaction> findByAccountNumber(String accountNumber) {
        return transactionHistory.getOrDefault(accountNumber, List.of());
    }
}
