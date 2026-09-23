package org.bee.banking.service;

import org.bee.banking.component.AccountMapper;
import org.bee.banking.component.WithdrawalMapper;
import org.bee.banking.domain.Account;
import org.bee.banking.domain.AccountType;
import org.bee.banking.domain.DepositForm;
import org.bee.banking.exception.AgeException;
import org.bee.banking.exception.MaxDepositAmountException;
import org.bee.banking.repository.AccountRepository;
import org.bee.banking.repository.TransactionRepository;
import org.bee.banking.repository.WithdrawalRepository;
import org.bee.banking.request.AccountRegistrationRequest;
import org.bee.banking.request.WithdrawalRequest;
import org.bee.banking.rules.AccountConstraints;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the banking business rules in ClientAccountService: age gating on
 * registration, account-number/prefix validation on withdraw/deposit, and account
 * closure. Collaborators are mocked or (for the pure mappers/in-memory repositories)
 * used as real instances, so no Spring context or MySQL is needed.
 */
@ExtendWith(MockitoExtension.class)
class ClientAccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private NotificationService notificationService;

    private ClientAccountService clientAccountService;
    private AccountConstraints accountConstraints;

    @BeforeEach
    void setUp() {
        accountConstraints = AccountConstraints.builder()
                .minimumAge(18)
                .maximumDepositAmountByCash(new BigDecimal("500.00"))
                .checkingMinimumBalance(BigDecimal.ZERO)
                .savingMinimumBalance(BigDecimal.ZERO)
                .build();

        clientAccountService = new ClientAccountService(
                accountRepository,
                AccountMapper.INSTANCE,
                WithdrawalMapper.INSTANCE,
                new WithdrawalRepository(),
                new TransactionRepository(),
                accountConstraints,
                notificationService);
    }

    private AccountRegistrationRequest.AccountRegistrationRequestBuilder validRegistrationRequestBuilder() {
        return AccountRegistrationRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .street("1 Main St")
                .city("Austin")
                .state("TX")
                .zip("78701")
                .addressLine1("1 Main St")
                .accountType("CHECKING");
    }

    @Test
    void registerNewClientAccount_belowMinimumAge_throwsAgeExceptionAndDoesNotSave() {
        AccountRegistrationRequest request = validRegistrationRequestBuilder()
                .dateOfBirth(LocalDate.now().minusYears(10))
                .build();

        assertThatThrownBy(() -> clientAccountService.registerNewClientAccount(request))
                .isInstanceOf(AgeException.class);

        verifyNoInteractions(accountRepository, notificationService);
    }

    @Test
    void registerNewClientAccount_meetsMinimumAge_savesAccountAndNotifiesCustomer() {
        AccountRegistrationRequest request = validRegistrationRequestBuilder()
                .dateOfBirth(LocalDate.now().minusYears(30))
                .build();
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Account result = clientAccountService.registerNewClientAccount(request);

        assertThat(result.getAccountType()).isEqualTo(AccountType.CHECKING);
        assertThat(result.getCustomer().getFirstName()).isEqualTo("John");
        verify(notificationService).sendEmail("John Doe");
        verify(notificationService).sendSms("John Doe");
    }

    @Test
    void closeAccount_delegatesToRepositoryAndReturnsItsResult() {
        Account closedAccount = Account.builder().checkingAccountNumber("CH-123").build();
        when(accountRepository.closeAccount("CH-123")).thenReturn(closedAccount);

        Account result = clientAccountService.closeAccount("CH-123");

        assertThat(result).isSameAs(closedAccount);
    }

    @Test
    void withdraw_accountNumberTooShort_throwsIllegalArgumentExceptionWithoutTouchingRepository() {
        WithdrawalRequest request = new WithdrawalRequest("C", AccountType.CHECKING, null,
                BigDecimal.TEN, null, null, null, null, null, null, null, null, null);

        assertThatThrownBy(() -> clientAccountService.withdrawAndSaveToAccount(request))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(accountRepository);
    }

    @Test
    void withdraw_nonPositiveAmount_throwsIllegalArgumentException() {
        WithdrawalRequest request = new WithdrawalRequest("CH-100", AccountType.CHECKING, null,
                BigDecimal.ZERO, null, null, null, null, null, null, null, null, null);

        assertThatThrownBy(() -> clientAccountService.withdrawAndSaveToAccount(request))
                .isInstanceOf(IllegalArgumentException.class);

        verify(accountRepository, never()).withdraw(any(), any(), any());
    }

    @Test
    void withdraw_unrecognizedAccountNumberPrefix_throwsIllegalArgumentException() {
        WithdrawalRequest request = new WithdrawalRequest("XX-100", AccountType.CHECKING, null,
                BigDecimal.TEN, null, null, null, null, null, null, null, null, null);

        assertThatThrownBy(() -> clientAccountService.withdrawAndSaveToAccount(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void withdraw_requestedTypeDoesNotMatchAccountNumberPrefix_throwsIllegalArgumentException() {
        WithdrawalRequest request = new WithdrawalRequest("CH-100", AccountType.SAVINGS, null,
                BigDecimal.TEN, null, null, null, null, null, null, null, null, null);

        assertThatThrownBy(() -> clientAccountService.withdrawAndSaveToAccount(request))
                .isInstanceOf(IllegalArgumentException.class);

        verify(accountRepository, never()).withdraw(any(), any(), any());
    }

    @Test
    void deposit_invalidDepositType_throwsIllegalArgumentException() {
        DepositForm form = new DepositForm("CH-100", BigDecimal.TEN, AccountType.CHECKING, "wire",
                null, null, null, null, null, null, null, null);

        assertThatThrownBy(() -> clientAccountService.depositAndSaveToAccount(form))
                .isInstanceOf(IllegalArgumentException.class);

        verify(accountRepository, never()).deposit(any(), any(), any());
    }

    @Test
    void deposit_cashAmountExceedsConfiguredMaximum_throwsMaxDepositAmountException() {
        DepositForm form = new DepositForm("CH-100", new BigDecimal("600.00"), AccountType.CHECKING, "cash",
                null, null, null, null, null, null, null, null);

        assertThatThrownBy(() -> clientAccountService.depositAndSaveToAccount(form))
                .isInstanceOf(MaxDepositAmountException.class);

        verify(accountRepository, never()).deposit(any(), any(), any());
    }
}
