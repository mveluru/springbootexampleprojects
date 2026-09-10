package org.bee.banking.service;

import org.bee.banking.domain.Address;
import org.bee.banking.domain.client;
import org.springframework.stereotype.Service;

@Service
public class ClientService {
    private client client;
    public client getClient(){
        Address address = Address.builder().street("111").city("Leander").state("Tx").zip("78717").country("USA").AddressLine1("Leafvillage").AddressLine2("Unit1").build();
        return  new client("MM","DD",address);
    }
}
