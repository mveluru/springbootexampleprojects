package org.bee.banking.gateway;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Registers {@link BankingRateLimitFilter} scoped to only the banking module's controller
 * paths (see each controller's @RequestMapping under org.bee.banking.contoller), so retail,
 * events, and configs endpoints are unaffected. The filter isn't a @Component itself so
 * Spring Boot doesn't also auto-register it for "/*".
 */
@Configuration
@RequiredArgsConstructor
public class BankingGatewayConfig {
    private final RateLimitProperties rateLimitProperties;
    private final CustomerRateLimiter customerRateLimiter;

    @Bean
    public FilterRegistrationBean<BankingRateLimitFilter> bankingRateLimitFilter() {
        FilterRegistrationBean<BankingRateLimitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new BankingRateLimitFilter(rateLimitProperties, customerRateLimiter));
        registration.setName("bankingRateLimitFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns(
                "/v1/api/accounts/*",
                "/v1/client/*",
                "/v1/payment/*",
                "/notify",
                "/notify-sms",
                "/report");
        return registration;
    }
}
