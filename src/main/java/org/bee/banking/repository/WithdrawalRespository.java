package org.bee.banking.repository;

import lombok.Getter;
import org.bee.banking.domain.Withdrawal;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Repository
public class WithdrawalRespository {
    private final Map<String, List<Withdrawal>> WithDrawalHistory = new ConcurrentHashMap<>();

    public void createWithdrawal(Withdrawal withdrawal) {
        Assert.notNull(withdrawal, "withdrawal must not be null");
    }
}
