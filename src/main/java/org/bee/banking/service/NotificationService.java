package org.bee.banking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bee.banking.messages.BankingMessages;
import org.bee.configs.config.BriteEmailConfigValues;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final BriteEmailConfigValues emailConfigValues;

    // 1. Fire-and-Forget (void return type)
    @Async
    public void sendEmail(String customer) {
        if (!emailConfigValues.isEnabled()) {
            log.info(BankingMessages.LOG_EMAIL_DISABLED, customer);
            return;
        }
        try {
            Thread.sleep(3000); // Simulate 3-second delay
            log.info(BankingMessages.LOG_EMAIL_SENT, customer, Thread.currentThread().getName());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn(BankingMessages.LOG_EMAIL_INTERRUPTED, customer);
        }
    }

    // 2. Async method returning a result
    @Async
    public CompletableFuture<String> fetchReportData() {
        try {
            Thread.sleep(3000); // Simulate delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn(BankingMessages.LOG_REPORT_INTERRUPTED);
        }
        log.info(BankingMessages.LOG_REPORT_GENERATED);
        return CompletableFuture.completedFuture("Report generated successfully!");
    }
}
