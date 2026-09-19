package org.bee.banking.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.io.Serializable;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Address implements Serializable {
    @NotBlank
    private String street;
    @NotBlank(message = "City is required")
    @Size(min = 1, max = 50)
    @Pattern(regexp = "^[a-zA-z0-9 .-]+$",message = "Invalid characters in city name")
    private String city;
    @NotBlank
    @NotBlank(message = "State is required")
    @Size(min = 2, max = 2, message = "State must be exactly 2 characters (e.g., TX)")
    private String state;
    @NotBlank
    @Pattern(regexp = "^\\d{5}$",message = "Non negative numbers")
    @NotBlank(message="Zip code is required")
    private String zip;
    @Builder.Default
    private String country="USA";
    @NotBlank
    @Size(min = 1, max = 50)
    private String addressLine1;
    @Size(min = 0, max = 50)
    private String addressLine2;


}
