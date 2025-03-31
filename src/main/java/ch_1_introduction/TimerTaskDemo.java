package ch_1_introduction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates the usage of TimerTask for scheduling tasks
 * Shows different scheduling patterns:
 * 1. One-time execution after delay
 * 2. Fixed-rate execution (starts at fixed time intervals)
 * 3. Fixed-delay execution (starts after previous execution completes)
 */
public class TimerTaskDemo {
    private static final Logger logger = LogManager.getLogger(TimerTaskDemo.class);
    private final Timer timer;

    public TimerTaskDemo() {
        this.timer = new Timer();
    }

    /**
     * Demonstrates one-time execution after a delay
     */
    private void demonstrateOneTimeExecution() {
        logger.info("Scheduling one-time task to execute after 2 seconds...");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                logger.info("One-time task executed at: {}", System.currentTimeMillis());
            }
        }, 2000); // 2 seconds delay
    }

    /**
     * Demonstrates fixed-rate execution
     * Task will start every 2 seconds regardless of execution time
     */
    private void demonstrateFixedRateExecution() {
        logger.info("Scheduling fixed-rate task to execute every 2 seconds...");
        timer.scheduleAtFixedRate(new TimerTask() {
            private int count = 0;

            @Override
            public void run() {
                count++;
                logger.info("Fixed-rate task execution #{} at: {}", count, System.currentTimeMillis());
                if (count >= 5) {
                    this.cancel(); // Stop after 5 executions
                }
            }
        }, 0, 2000); // Initial delay: 0, period: 2 seconds
    }

    /**
     * Demonstrates fixed-delay execution
     * Task will start 2 seconds after previous execution completes
     */
    private void demonstrateFixedDelayExecution() {
        logger.info("Scheduling fixed-delay task to execute with 2-second delay between executions...");
        timer.schedule(new TimerTask() {
            private int count = 0;

            @Override
            public void run() {
                count++;
                logger.info("Fixed-delay task execution #{} starting at: {}", count, System.currentTimeMillis());
                try {
                    // Simulate some work that takes 1 second
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.error("Task interrupted", e);
                }
                logger.info("Fixed-delay task execution #{} completed at: {}", count, System.currentTimeMillis());
                if (count >= 5) {
                    this.cancel(); // Stop after 5 executions
                }
            }
        }, 0, 2000); // Initial delay: 0, period: 2 seconds
    }

    /**
     * Demonstrates task cancellation
     */
    private void demonstrateTaskCancellation() {
        logger.info("Scheduling task to demonstrate cancellation...");
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                logger.info("This task will be cancelled after 3 seconds");
            }
        };
        timer.schedule(task, 0, 1000); // Execute every second

        // Cancel the task after 3 seconds
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                task.cancel();
                logger.info("Task cancelled");
            }
        }, 3000);
    }

    /**
     * Shuts down the timer
     */
    public void shutdown() {
        timer.cancel();
        logger.info("Timer cancelled");
    }

    public static void main(String[] args) {
        TimerTaskDemo demo = new TimerTaskDemo();
        try {
            // Demonstrate different scheduling patterns
            demo.demonstrateOneTimeExecution();
            TimeUnit.SECONDS.sleep(3); // Wait for one-time task to complete

            demo.demonstrateFixedRateExecution();
            TimeUnit.SECONDS.sleep(12); // Wait for fixed-rate task to complete

            demo.demonstrateFixedDelayExecution();
            TimeUnit.SECONDS.sleep(12); // Wait for fixed-delay task to complete

            demo.demonstrateTaskCancellation();
            TimeUnit.SECONDS.sleep(4); // Wait for cancellation demo to complete
        } catch (InterruptedException e) {
            logger.error("Main thread interrupted", e);
            Thread.currentThread().interrupt(); // Restore interrupted status
        } finally {
            // Ensure timer is always cancelled, even if an exception occurs
            demo.shutdown();
        }
    }
} 