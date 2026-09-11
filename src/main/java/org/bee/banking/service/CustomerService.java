package org.bee.banking.service;

import org.bee.banking.domain.Address;
import org.bee.banking.domain.Customer;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {
    private Customer client;
    public Customer getCustomer(){
        Address address = Address.builder().street("111").city("Leander").state("Tx").zip("78717").country("USA").addressLine1("Leafvillage").addressLine2("Unit1").build();
        return  new Customer("MM","DD",address);
    }
}
