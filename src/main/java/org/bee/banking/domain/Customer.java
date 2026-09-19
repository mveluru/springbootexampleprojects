package org.bee.banking.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.bee.banking.messages.StaticMessages;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer implements Serializable {
    @Size(min = 1, max = 50)
    @Pattern(regexp = "[a-zA-Z]+$",message = StaticMessages.VALIDATION_NAME_LETTERS_ONLY_SHORT)
    String firstName;
    @Size(min = 1, max = 25)
    @Pattern(regexp = "[a-zA-Z]+$",message = StaticMessages.VALIDATION_NAME_LETTERS_ONLY_PROPER)
    String lastName;
    Address address;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy")
    @NonNull
    private LocalDate dateOfBirth;


    // The method name must start with "is" for the validator to pick it up automatically
    @AssertTrue(message = StaticMessages.VALIDATION_DOB_YEAR_1940)
    public boolean isDateOfBirthValid() {
        return dateOfBirth != null && dateOfBirth.getYear() >= 1940;
    }

    public Customer(@NonNull  String firstName, @NonNull String lastName, Address address) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
    }


}
