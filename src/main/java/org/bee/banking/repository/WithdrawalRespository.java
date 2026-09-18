package org.bee.banking.repository;

import org.bee.banking.domain.Withdrawal;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class WithdrawalRespository {
    private final Map<String, List<Withdrawal>> WithDrawalHistory = new ConcurrentHashMap<>();

    public Map<String, List<Withdrawal>> getWithDrawalHistory() {
        return WithDrawalHistory;
    }

    public void createWithdrawal(Withdrawal withdrawal) {
        Assert.notNull(withdrawal, "withdrawal must not be null");
    }
}
