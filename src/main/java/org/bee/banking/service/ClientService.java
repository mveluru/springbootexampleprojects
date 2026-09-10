package org.bee.banking.service;

import org.bee.banking.domain.client;
import org.springframework.stereotype.Service;

@Service
public class ClientService {
    private client client;
    public client getClient(){
        return  new client("MM","DD");
    }
}
