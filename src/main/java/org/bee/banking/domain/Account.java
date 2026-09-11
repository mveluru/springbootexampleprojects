package org.bee.banking.domain;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account implements Serializable {
    String checkingAccountNumber;
    String savingAccountNumber;
    BigDecimal checkingBalance;
    BigDecimal savingBalance;
    @Enumerated(EnumType.STRING)
    private AccountType accountType;
    Customer customer;
}
