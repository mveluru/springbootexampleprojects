package org.bee.configs.controller;

import org.bee.configs.service.BriteConfigValuesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/configs")
public class BriteConfigValuesController {
    @Autowired
    public BriteConfigValuesService briteConfigValuesService;

    @GetMapping("/app-config/values")
    public Map<String, String> showRawAppConfig() {
        return briteConfigValuesService.applicationConfigValues();

    }

    @GetMapping("/email-config/values")
    public Map<String, String> showEmailConfig() {
        return briteConfigValuesService.emailConfigValues();
    }

    @GetMapping("/sms-config/values")
    public Map<String, String> sendSmsNotification() {
        return briteConfigValuesService.smsConfigValues();
    }
}
