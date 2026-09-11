package org.bee.banking.request;

import lombok.Data;

@Data
public class AccountLookupRequest {
    private String accountNumber;
}
