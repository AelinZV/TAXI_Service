package com.taxi.notificationservice.service;

import com.taxi.notificationservice.entity.NotificationTask;
import com.taxi.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.Executor;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationWorker {

    private final NotificationRepository repository;
    private final Executor taskExecutor;  // ← Изменено: Executor вместо ThreadPoolTaskExecutor

    @Value("${worker.max-retries:3}")
    private int maxRetries;

    @Scheduled(fixedDelay = 2000)
    public void pollAndDispatch() {
        List<NotificationTask> tasks = repository.findPendingTasks();
        for (NotificationTask task : tasks) {
            taskExecutor.execute(() -> processTask(task.getId()));
        }
    }

    @Transactional
    public void processTask(Long taskId) {
        int claimed = repository.claimTask(taskId);
        if (claimed == 0) {
            log.debug("Task {} already claimed by another worker", taskId);
            return;
        }

        log.info("Processing notification task {}", taskId);

        try {
            simulateSend();
            repository.updateStatus(taskId, "SENT");
            log.info("Task {} sent successfully", taskId);

        } catch (Exception e) {
            log.error("Task {} failed: {}", taskId, e.getMessage());
            repository.incrementAttempts(taskId);
            Integer attempts = repository.getAttempts(taskId);

            if (attempts != null && attempts >= maxRetries) {
                repository.updateStatus(taskId, "FAILED");
                log.warn("Task {} failed permanently after {} attempts", taskId, attempts);
            } else {
                repository.updateStatus(taskId, "PENDING");
                log.info("Task {} re-queued (attempt {}/{})", taskId,
                        attempts != null ? attempts : 0, maxRetries);
            }
        }
    }

    private void simulateSend() throws InterruptedException {
        Thread.sleep(100);
        if (Math.random() < 0.1) {
            throw new RuntimeException("Simulated random failure");
        }
    }

    @PreDestroy
    public void gracefulShutdown() {
        log.info("Notification workers shutting down gracefully...");
        // Executor не имеет метода shutdown(), поэтому приводим к ThreadPoolTaskExecutor
        if (taskExecutor instanceof ThreadPoolTaskExecutor threadPool) {
            threadPool.shutdown();
        }
    }
}