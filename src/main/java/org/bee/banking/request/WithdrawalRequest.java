package org.bee.banking.request;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.bee.banking.domain.AccountType;
import org.bee.banking.domain.Address;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalRequest {
    String AccountNumber;

    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    Date withdrawalDate;
    BigDecimal withdrawAmount;
    String withdrawalStatus;

    @Size(min = 1, max = 50)
    @Pattern(regexp = "[a-zA-Z]+$",message = "matches one or more letters, no spaces no special chars")
    String firstName;

    @Size(min = 1, max = 25)
    @Pattern(regexp = "[a-zA-Z]+$",message = "matches one or more letters, no spaces ,no special chars")
    String lastName;

    @NotBlank(message = "Street address is required")
    private String street;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    @Size(min = 2, max = 2, message = "State must be exactly 2 characters (e.g., TX)")
    private String state;

    @NotBlank(message = "Zip code is required")
    private String zip;

    private String addressLine1;
    private String addressLine2;


}

