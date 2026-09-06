package org.bee.orders.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notification.retry")
@Getter
@Setter
public class BriteOrderRetryConfigValues {

    private int maxAttempts;
    private long backoffMillis;
}
