package org.bee.orders.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notification.sms")
@Getter
@Setter
public class BriteOrderSmsNotificationConfigValues {
    private boolean enabled;
    private String senderId;
    private int dailyLimit;
}
