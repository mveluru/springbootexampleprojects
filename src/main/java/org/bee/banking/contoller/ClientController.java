package org.bee.banking.contoller;

import org.bee.banking.domain.client;
import org.bee.banking.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/client")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @GetMapping("/name")
    public @ResponseBody client getClient(){
        return clientService.getClient();
    }
}
