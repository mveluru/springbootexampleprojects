package org.bee.banking.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
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
    @Pattern(regexp = "^[a-zA-z0-9 .-_]+$",message = "Invalid characters in address line1")
    private String addressLine1;
    @Pattern(regexp = "^[a-zA-Z0-9 ._-]+$")
    @Size(min = 0, max = 50)
    private String addressLine2;

    @NotBlank(message = "City is required")
    @Size(min = 1, max = 50)
    @Pattern(regexp = "^[a-zA-z0-9 .-]+$",message = "Invalid characters in city name")
    private String city;

    @NotBlank(message = "State is required")
    @Size(min = 2, max = 2, message = "State must be exactly 2 characters (e.g., TX)")
    private String state;

    /*
    In Bean Validation's @Pattern, the regex engine evaluates the expression using Matcher.matches().
    This implicitly matches the entire string. However, explicitly adding ^ (start of string) and $ (end of string)
    is a best practice in validation frameworks to guarantee no leading or trailing characters sneak past
     */
    @NotBlank(message = "Zip code is required")
    @Pattern(regexp = "^\\d{5}$",message ="zip code must be exactly 5 digits")
    private String zip;


}
