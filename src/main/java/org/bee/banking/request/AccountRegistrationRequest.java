package org.bee.banking.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bee.banking.messages.StaticMessages;
import org.hibernate.validator.constraints.Range;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountRegistrationRequest {
    @NotBlank(message = StaticMessages.VALIDATION_FIRST_NAME_REQUIRED)
    @Size(max = 50, message = StaticMessages.VALIDATION_FIRST_NAME_MAX_LENGTH)
    private String firstName;

    @NotBlank(message = StaticMessages.VALIDATION_LAST_NAME_REQUIRED)
    @Size(max = 50, message = StaticMessages.VALIDATION_LAST_NAME_MAX_LENGTH)
    private String lastName;

    @NotNull(message = StaticMessages.VALIDATION_DOB_REQUIRED)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy")
    private LocalDate dateOfBirth;

    @NotBlank(message = StaticMessages.VALIDATION_STREET_REQUIRED)
    private String street;

    @NotBlank(message = StaticMessages.VALIDATION_CITY_REQUIRED)
    private String city;

    @NotBlank(message = StaticMessages.VALIDATION_STATE_REQUIRED)
    @Size(min = 2, max = 2, message = StaticMessages.VALIDATION_STATE_LENGTH)
    private String state;

    @NotBlank(message = StaticMessages.VALIDATION_ZIP_REQUIRED)
    @Pattern(regexp = "\\d+")
    private String zip;

    @NotBlank(message = StaticMessages.VALIDATION_ADDRESS_LINE1_REQUIRED)
    @Size(min = 1, max = 50, message = StaticMessages.VALIDATION_ADDRESS_LINE1_REQUIRED)
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = StaticMessages.VALIDATION_ACCOUNT_TYPE_REQUIRED)
    private String accountType; // Accepts raw text string like "checking" or "SAVINGS"

    // Custom helper method to enforce the 1940 cutoff rule automatically
    @Range(min = 1940, max = 3000, message = StaticMessages.VALIDATION_DOB_YEAR_MIN)
    public int getDateOfBirthYear() {
        return dateOfBirth != null ? dateOfBirth.getYear() : 1940;
    }
}
