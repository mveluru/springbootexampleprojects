package org.bee.banking.component;

import org.springframework.stereotype.Component;

@Component
public class BankClient {

    public String processPayment(){
        return "Successfully Processed";
    }
}
