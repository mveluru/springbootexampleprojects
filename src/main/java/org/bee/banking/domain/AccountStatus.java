package org.bee.banking.domain;

import java.util.Date;

public class AccountStatus {
    String accountNumber;
    String accountType;
    String accountStatus;// ACTIVE , CLOSED
    Date createdDate;// Not EMPTY
    Date closedDate; // iF ACTIVE closedDATE will be empty
}
