package org.bee.orders.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "notification")
@Validated
@Getter
@Setter
public class BriteOrderNotificationConfigValues {

    @Valid
    private Email emailValues;
    private Sms smsValues;
    private Retry retryValues;
    @Min(1)
    private int timeoutSeconds;
    @Min(1)
    private int connectionPoolSize;
    private Map<String, String> templates;

    @Valid
    @Getter
    @Setter
    public static class Email {
        private boolean enabled;
        @NotBlank
        private String fromAddress;
        private String supportAddress;
        private int dailyLimit;
        private List<String> defaultRecipients;
    }

    @Valid
    @Getter
    @Setter
    public static class Sms {
        private boolean enabled;
        private String senderId;
        private int dailyLimit;
    }

    @Valid
    @Getter
    @Setter
    public static class Retry {
        private int maxAttempts;
        private long backoffMillis;
    }

}
