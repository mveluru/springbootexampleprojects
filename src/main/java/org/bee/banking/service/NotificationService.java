package org.bee.banking.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class NotificationService {

    // 1. Fire-and-Forget (void return type)
    @Async
    public void sendEmail(String user) {
        try {
            Thread.sleep(3000); // Simulate 3-second delay
            System.out.println("Email sent to " + user + " on thread: " + Thread.currentThread().getName());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // 2. Async method returning a result
    @Async
    public CompletableFuture<String> fetchReportData() {
        try {
            Thread.sleep(2000); // Simulate delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return CompletableFuture.completedFuture("Report generated successfully!");
    }
}
