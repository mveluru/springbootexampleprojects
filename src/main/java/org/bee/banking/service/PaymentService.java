package org.bee.banking.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.bee.banking.component.BankClient;
import org.springframework.stereotype.Service;
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
            return bankClient.processPayment();
        }

        public String paymentFallback(Throwable ex) {
            return "Payment service temporarily unavailable";
        }

}
