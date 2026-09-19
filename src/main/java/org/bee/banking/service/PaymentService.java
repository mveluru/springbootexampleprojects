package org.bee.banking.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.bee.banking.component.BankClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentService {
      private final BankClient bankClient;

        public PaymentService(BankClient bankClient) {
            this.bankClient = bankClient;
        }

        @CircuitBreaker(
                name = "bankService",
                fallbackMethod = "paymentFallback"
        )
        public String processPayment() {
            log.debug("Processing payment via BankClient");
            return bankClient.processPayment();
        }

        public String paymentFallback(Throwable ex) {
            log.warn("Payment circuit breaker fallback triggered: {}", ex.getMessage());
            return "Payment service temporarily unavailable";
        }

}
