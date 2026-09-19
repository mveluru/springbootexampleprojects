package org.bee.banking.service;

import lombok.extern.slf4j.Slf4j;
import org.bee.banking.domain.Address;
import org.bee.banking.domain.Customer;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
public class CustomerService {
    private Customer client;
    public Customer getCustomer(){
        log.debug("Returning sample customer record");
        Address address = Address.builder().street("111").city("Leander").state("Tx").zip("78717").country("USA").addressLine1("Leafvillage").addressLine2("Unit1").build();
        return new Customer("MM", "DD", address, LocalDate.of(1990, 1, 1));
    }
}
