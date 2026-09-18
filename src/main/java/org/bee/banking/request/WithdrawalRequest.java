package org.bee.banking.request;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.bee.banking.domain.AccountType;
import org.bee.banking.domain.Address;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class WithdrawalRequest {
    String AccountNumber;
    @Enumerated(EnumType.STRING)
    private AccountType accountType;
    @Size(min = 1, max = 50)
    @Pattern(regexp = "[a-zA-Z]+$",message = "matches one or more letters, no spaces no special chars")
    String firstName;
    @Size(min = 1, max = 25)
    @Pattern(regexp = "[a-zA-Z]+$",message = "matches one or more letters, no spaces ,no special chars")
    String lastName;
    Address address;
    Date withdrawalDate;
    BigDecimal withdrawAmount;
    String withdrawalStatus;
}

