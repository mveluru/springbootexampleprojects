package org.bee.banking.domain;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"checkingAccountNumber", "savingAccountNumber"})
public class Account implements Serializable {
    String checkingAccountNumber;
    String savingAccountNumber;
    BigDecimal checkingBalance;
    BigDecimal savingBalance;
    @Enumerated(EnumType.STRING)
    private AccountType accountType;
    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;
    private LocalDate createdDate;
    private LocalDate closedDate;
    Customer customer;
}
