package org.bee.banking.domain;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@AllArgsConstructor
@Data
public class WithDraw {

    String AccountNumber;
    @Enumerated(EnumType.STRING)
    private AccountType accountType;
    @Size(min = 1, max = 50)
    @Pattern(regexp = "[a-zA-Z]",message = "no special chars")
    String firstName;
    @Size(min = 1, max = 25)
    @Pattern(regexp = "[a-zA-Z]",message = "no special chars")
    String lastName;
    Address address;
    Date withdrawalDate;
    BigDecimal withdrawAmount;

}
