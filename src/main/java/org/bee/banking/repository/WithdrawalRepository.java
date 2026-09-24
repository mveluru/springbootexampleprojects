package org.bee.banking.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bee.banking.domain.Address;
import org.bee.banking.domain.WithdrawalForm;
import org.bee.banking.entity.AccountEntity;
import org.bee.banking.entity.AddressEmbeddable;
import org.bee.banking.entity.WithdrawalHistoryEntity;
import org.bee.banking.exception.AccountNotFoundException;
import org.bee.banking.messages.BankingMessages;
import org.bee.banking.repository.jpa.AccountJpaRepository;
import org.bee.banking.repository.jpa.WithdrawalHistoryJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * JPA-backed facade over {@link WithdrawalHistoryJpaRepository}, replacing the old
 * in-memory {@code Map<accountNumber, List<WithdrawalForm>>}. Write-only, same as
 * before the JPA migration - nothing currently reads this history back.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class WithdrawalRepository {
    private final WithdrawalHistoryJpaRepository withdrawalHistoryJpaRepository;
    private final AccountJpaRepository accountJpaRepository;

    @Transactional
    public void createWithdrawal(WithdrawalForm withdrawalForm) {
        Assert.notNull(withdrawalForm, "withdrawal must not be null");
        AccountEntity account = accountJpaRepository.findByAccountNumber(withdrawalForm.getAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException(
                        String.format(BankingMessages.ACCOUNT_NOT_FOUND, withdrawalForm.getAccountNumber())));
        WithdrawalHistoryEntity entity = WithdrawalHistoryEntity.builder()
                .account(account)
                .withdrawalDate(withdrawalForm.getWithdrawalDate())
                .amount(withdrawalForm.getWithdrawalAmount())
                .status(withdrawalForm.getWithdrawalStatus())
                .firstName(withdrawalForm.getFirstName())
                .lastName(withdrawalForm.getLastName())
                .address(toEntity(withdrawalForm.getAddress()))
                .build();
        withdrawalHistoryJpaRepository.save(entity);
        log.info(BankingMessages.LOG_WITHDRAWAL_HISTORY_RECORDED,
                withdrawalForm.getAccountNumber(), withdrawalForm.getWithdrawalAmount(), withdrawalForm.getWithdrawalStatus());
    }

    private AddressEmbeddable toEntity(Address address) {
        if (address == null) return null;
        return AddressEmbeddable.builder()
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .zip(address.getZip())
                .country(address.getCountry())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .build();
    }
}
