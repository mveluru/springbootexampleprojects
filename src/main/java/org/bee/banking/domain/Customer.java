package org.bee.banking.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class Customer {
    @Size(min = 1, max = 50)
    @Pattern(regexp = "[a-zA-Z]",message = "no special chars")
    String firstName;
    @Size(min = 1, max = 25)
    @Pattern(regexp = "[a-zA-Z]",message = "no special chars")
    String lastName;
    Address address;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy")
    @NonNull
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    // The method name must start with "is" for the validator to pick it up automatically
    @AssertTrue(message = "Date of birth must be from year 1940 onwards")
    public boolean isDateOfBirthValid() {
        return dateOfBirth.getYear() >= 1940;
    }

    public Customer(@NonNull  String firstName, @NonNull String lastName, Address address) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
    }


}
