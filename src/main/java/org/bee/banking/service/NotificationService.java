package org.bee.banking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class NotificationService {

    // 1. Fire-and-Forget (void return type)
    @Async
    public void sendEmail(String user) {
        try {
            Thread.sleep(3000); // Simulate 3-second delay
            log.info("Email sent to {} on thread: {}", user, Thread.currentThread().getName());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Email send to {} interrupted", user);
        }
    }

    // 2. Async method returning a result
    @Async
    public CompletableFuture<String> fetchReportData() {
        try {
            Thread.sleep(2000); // Simulate delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Report generation interrupted");
        }
        log.info("Report generated successfully");
        return CompletableFuture.completedFuture("Report generated successfully!");
    }
}
