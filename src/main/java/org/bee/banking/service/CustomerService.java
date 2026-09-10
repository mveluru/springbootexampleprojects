package org.bee.banking.service;

import org.bee.banking.domain.Address;
import org.bee.banking.domain.Customer;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {
    private Customer client;
    public Customer getCustomer(){
        Address address = Address.builder().street("111").city("Leander").state("Tx").zip("78717").country("USA").AddressLine1("Leafvillage").AddressLine2("Unit1").build();
        return  new Customer("MM","DD",address);
    }
}
