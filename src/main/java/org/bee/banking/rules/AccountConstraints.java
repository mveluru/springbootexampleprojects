package org.bee.banking.rules;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
public class AccountConstraints {

    BigDecimal maximumDepositAmountByCash; // property value
    String Age ;// to create account , deposit   age should be greater than 18
    BigDecimal checkingMinimumBalance; // property value
    BigDecimal savingMaximumBalance; // property value
}
