package org.bee.orders.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notification")
@Getter
@Setter
public class BriteOrderApplicationConfigValues {
    private int timeoutSeconds;
    private int connectionPoolSize;
}
