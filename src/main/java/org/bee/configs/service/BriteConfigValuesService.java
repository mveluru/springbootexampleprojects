package org.bee.configs.service;

import lombok.extern.slf4j.Slf4j;
import org.bee.configs.config.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public Map<String,String> applicationConfigValues() {
        return Map.of(
                "connectionPoolSize", String.valueOf(briteApplicationConfigValues.getConnectionPoolSize()),
                "timeoutInSeconds", String.valueOf(briteApplicationConfigValues.getTimeoutSeconds())
        );
    }

    public Map<String,String> emailConfigValues() {
        return Map.of(
                "enabled", String.valueOf(briteEmailConfigValues.isEnabled()),
                "fromAddress", String.valueOf(briteEmailConfigValues.getFromAddress()),
                "supportAddress", String.valueOf(briteEmailConfigValues.getSupportAddress()),
                "dailyLimit", String.valueOf(briteEmailConfigValues.getDailyLimit())
        );
    }

    public Map<String,String> smsConfigValues() {
        return Map.of(
                "enabled", String.valueOf(briteSmsNotificationConfigValues.isEnabled()),
                "sender-id", String.valueOf(briteSmsNotificationConfigValues.getSenderId()),
                "dailyLimit", String.valueOf(briteSmsNotificationConfigValues.getDailyLimit())
        );
    }
}
