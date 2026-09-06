package org.bee.orders.service;

import lombok.extern.slf4j.Slf4j;
import org.bee.orders.config.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class BriteOrderServicConfigValues {
    @Autowired
    private BriteOrderApplicationConfigValues briteOrderApplicationConfigValues;

    @Autowired
    private BriteOrderEmailConfigValues briteOrderEmailConfigValues;
    @Autowired
    private BriteOrderNotificationConfigValues briteOrderNotificationConfigValues;
    @Autowired
    private BriteOrderSmsNotificationConfigValues briteOrderSmsNotificationConfigValues;
    @Autowired
    private BriteOrderRetryConfigValues briteOrderRetryConfigValues;
    public static final Map<String,String> configValues= new HashMap<>();
    public static final Map<String,String> emailConfigValues= new HashMap<>();
    public static final Map<String,String> smsConfigValues= new HashMap<>();


    public  Map<String,String> applicationConfigValues() {
        configValues.put("connectionPoolSize", String.valueOf(briteOrderApplicationConfigValues.getConnectionPoolSize()));
        configValues.put("timeoutInSeconds",String.valueOf(briteOrderApplicationConfigValues.getTimeoutSeconds()));
      return configValues;
    }

    public  Map<String,String> emailConfigValues() {
        emailConfigValues.put("enabled", String.valueOf(briteOrderEmailConfigValues.isEnabled()));
        emailConfigValues.put("fromAddress",String.valueOf(briteOrderEmailConfigValues.getFromAddress()));
        emailConfigValues.put("supportAddress",String.valueOf(briteOrderEmailConfigValues.getSupportAddress()));
        emailConfigValues.put("dailyLimit",String.valueOf(briteOrderEmailConfigValues.getDailyLimit()));
        return emailConfigValues;
    }

    public  Map<String,String> smsConfigValues() {
        smsConfigValues.put("enabled", String.valueOf(briteOrderSmsNotificationConfigValues.isEnabled()));
        smsConfigValues.put("sender-id",String.valueOf(briteOrderSmsNotificationConfigValues.getSenderId()));
        smsConfigValues.put("dailyLimit",String.valueOf(briteOrderSmsNotificationConfigValues.getDailyLimit()));
        return smsConfigValues;
    }


}
