package org.bee.banking.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountRegistrationRequest {
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    @NotNull(message = "Date of birth is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy")
    private LocalDate dateOfBirth;

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

    @NotBlank(message = "Account type is required")
    private String accountType; // Accepts raw text string like "checking" or "SAVINGS"

    // Custom helper method to enforce the 1940 cutoff rule automatically
    @Range(min = 1940, max = 3000, message = "Year must be 1940 or later")
    public int getDateOfBirthYear() {
        return dateOfBirth != null ? dateOfBirth.getYear() : 1940;
    }
}
