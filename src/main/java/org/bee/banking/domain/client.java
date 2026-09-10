package org.bee.banking.domain;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.lang.NonNull;
import lombok.Getter;

import lombok.Setter;

@Getter
@Setter
public class client {
    @Size(min = 1, max = 100)
    @Pattern(regexp = "[a-zA-Z]",message = "no special chars")
    String firstName;

    @Size(min = 1, max = 100)
    @Pattern(regexp = "[a-zA-Z]",message = "no special chars")
    String lastName;
    public client(@NonNull  String firstName, @NonNull String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

}
