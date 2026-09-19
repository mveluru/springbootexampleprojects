package org.bee.configs.service;

import lombok.extern.slf4j.Slf4j;
import org.bee.configs.config.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class BriteConfigValuesService {
    @Autowired
    private BriteApplicationConfigValues briteApplicationConfigValues;

    @Autowired
    private BriteEmailConfigValues briteEmailConfigValues;
    @Autowired
    private BriteSmsNotificationConfigValues briteSmsNotificationConfigValues;
    public static final Map<String,String> configValues= new HashMap<>();
    public static final Map<String,String> emailConfigValues= new HashMap<>();
    public static final Map<String,String> smsConfigValues= new HashMap<>();


    public  Map<String,String> applicationConfigValues() {
        configValues.put("connectionPoolSize", String.valueOf(briteApplicationConfigValues.getConnectionPoolSize()));
        configValues.put("timeoutInSeconds",String.valueOf(briteApplicationConfigValues.getTimeoutSeconds()));
      return configValues;
    }

    public  Map<String,String> emailConfigValues() {
        emailConfigValues.put("enabled", String.valueOf(briteEmailConfigValues.isEnabled()));
        emailConfigValues.put("fromAddress",String.valueOf(briteEmailConfigValues.getFromAddress()));
        emailConfigValues.put("supportAddress",String.valueOf(briteEmailConfigValues.getSupportAddress()));
        emailConfigValues.put("dailyLimit",String.valueOf(briteEmailConfigValues.getDailyLimit()));
        return emailConfigValues;
    }

    public  Map<String,String> smsConfigValues() {
        smsConfigValues.put("enabled", String.valueOf(briteSmsNotificationConfigValues.isEnabled()));
        smsConfigValues.put("sender-id",String.valueOf(briteSmsNotificationConfigValues.getSenderId()));
        smsConfigValues.put("dailyLimit",String.valueOf(briteSmsNotificationConfigValues.getDailyLimit()));
        return smsConfigValues;
    }


}
