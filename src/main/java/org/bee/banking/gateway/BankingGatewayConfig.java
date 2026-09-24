package org.bee.banking.gateway;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Registers the banking module's gateway filters, scoped to only its controller paths
 * (see each controller's @RequestMapping under org.bee.banking.contoller), so retail,
 * events, and configs endpoints are unaffected. Neither filter is a @Component itself
 * so Spring Boot doesn't also auto-register them for "/*". {@link BusinessTransactionIdFilter}
 * runs first (lower order value = higher precedence) so every request - including ones
 * {@link BankingRateLimitFilter} goes on to reject - gets a correlatable btid.
 */
@Configuration
@RequiredArgsConstructor
public class BankingGatewayConfig {
    private static final String[] BANKING_URL_PATTERNS = {
            "/v1/api/accounts/*",
            "/v1/client/*",
            "/v1/payment/*",
            "/notify",
            "/notify-sms",
            "/report"
    };

    private final RateLimitProperties rateLimitProperties;
    private final CustomerRateLimiter customerRateLimiter;

    @Bean
    public FilterRegistrationBean<BusinessTransactionIdFilter> businessTransactionIdFilter() {
        FilterRegistrationBean<BusinessTransactionIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new BusinessTransactionIdFilter());
        registration.setName("businessTransactionIdFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns(BANKING_URL_PATTERNS);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<BankingRateLimitFilter> bankingRateLimitFilter() {
        FilterRegistrationBean<BankingRateLimitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new BankingRateLimitFilter(rateLimitProperties, customerRateLimiter));
        registration.setName("bankingRateLimitFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        registration.addUrlPatterns(BANKING_URL_PATTERNS);
        return registration;
    }
}
