package org.bee.banking.request;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.bee.banking.domain.AccountType;
import org.bee.banking.domain.Address;
import org.bee.banking.messages.StaticMessages;

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
    @Pattern(regexp = "^[a-zA-Z]+$",message = StaticMessages.VALIDATION_NAME_LETTERS_ONLY)
    String firstName;

    @Size(min = 1, max = 25)
    @Pattern(regexp = "^[a-zA-Z]+$",message = StaticMessages.VALIDATION_NAME_LETTERS_ONLY)
    String lastName;

    @NotBlank(message = StaticMessages.VALIDATION_STREET_REQUIRED)
    private String street;

    @NotBlank(message = StaticMessages.VALIDATION_CITY_REQUIRED)
    private String city;

    @NotBlank(message = StaticMessages.VALIDATION_STATE_REQUIRED)
    @Size(min = 2, max = 2, message = StaticMessages.VALIDATION_STATE_LENGTH)
    @Pattern(regexp = "^[A-Z]{2}$",message = StaticMessages.VALIDATION_STATE_UPPERCASE)
    private String state;

    @NotBlank(message = StaticMessages.VALIDATION_ZIP_REQUIRED)

    private String zip;

    private String addressLine1;
    private String addressLine2;


}

