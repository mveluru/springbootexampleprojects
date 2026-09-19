package org.bee.configs.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notification")
@Getter
@Setter
public class BriteApplicationConfigValues {
    private int timeoutSeconds;
    private int connectionPoolSize;
}
