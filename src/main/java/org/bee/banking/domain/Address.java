package org.bee.banking.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.bind.DefaultValue;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @NotBlank
    private String street;
    private String city;
    private String state;
    @NotBlank
    @Pattern(regexp = "[\\d+]]",message = "Non negative numbers")
    @Size(min = 5, max = 5)
    private String zip;
    private String country;
    @NotBlank
    private String AddressLine1;
    private String AddressLine2;


}
