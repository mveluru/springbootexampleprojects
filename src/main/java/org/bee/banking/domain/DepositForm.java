package org.bee.banking.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class DepositForm {
    String AccountNumber;
    BigDecimal amount;
    AccountType accountType;
    String  depositType; // cash or check

    @Size(min = 1, max = 50)
    @Pattern(regexp = "[a-zA-Z]+$",message = "matches one or more letters, no spaces no special chars")
    String firstName;

    @Size(min = 1, max = 25)
    @Pattern(regexp = "[a-zA-Z]+$",message = "matches one or more letters, no spaces ,no special chars")
    String lastName;

    @NotBlank(message = "Street address is required")
    private String street;

    @NotBlank(message = "address line1 required.")
    @Size(min = 1, max = 50)
    private String addressLine1;
    @Size(min = 1, max = 50)
    private String addressLine2;

    @NotBlank(message = "City is required")
    @Size(min = 1, max = 50)
    private String city;

    @NotBlank(message = "State is required")
    @Size(min = 2, max = 2, message = "State must be exactly 2 characters (e.g., TX)")
    private String state;

    @NotBlank(message = "Zip code is required")
    @Size(min =5, max = 5)
    @Pattern(regexp = "[0-9]+$")
    private String zip;


}
