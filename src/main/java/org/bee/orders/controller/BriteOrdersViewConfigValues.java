package org.bee.orders.controller;

import org.bee.orders.service.BriteOrderServicConfigValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/orders")
public class BriteOrdersViewConfigValues {
    @Autowired
    public BriteOrderServicConfigValues briteOrderServicConfigValues;;

    @GetMapping("/app-config/values")
    public Map<String, String> showRawAppConfig() {
        return briteOrderServicConfigValues.applicationConfigValues();

    }

    @GetMapping("/email-config/values")
    public Map<String, String> showEmailConfig() {
        return briteOrderServicConfigValues.emailConfigValues();
    }

    @GetMapping("/sms-confi/values")
    public Map<String, String> sendSmsNotification() {
        return briteOrderServicConfigValues.smsConfigValues();
    }
}
