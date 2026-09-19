package org.bee.banking.repository;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bee.banking.domain.WithdrawalForm;
import org.bee.banking.messages.BankingMessages;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@Slf4j
@Repository
public class WithdrawalRepository {
    private final Map<String, List<WithdrawalForm>> WithDrawalHistory = new ConcurrentHashMap<>();

    public void createWithdrawal(WithdrawalForm withdrawalForm) {
        Assert.notNull(withdrawalForm, "withdrawal must not be null");
        WithDrawalHistory
                .computeIfAbsent(withdrawalForm.getAccountNumber(), key -> new CopyOnWriteArrayList<>())
                .add(withdrawalForm);
        log.info(BankingMessages.LOG_WITHDRAWAL_HISTORY_RECORDED,
                withdrawalForm.getAccountNumber(), withdrawalForm.getWithdrawalAmount(), withdrawalForm.getWithdrawalStatus());
    }
}
