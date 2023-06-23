package ch7_cancellation_and_shutdown.logging_service_graceful_shutdown;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class LogService {

    private final BlockingQueue<String> queue;
    private final LoggerThread loggerThread;
    private boolean isShutdown;
    private int reservations;

    public LogService() {
        this.queue = new ArrayBlockingQueue<>(100);
        this.loggerThread = new LoggerThread();
        this.isShutdown = false;
        this.reservations = 0;
    }

    public void log(String msg) {
        synchronized (this) {
            if (isShutdown) {
                return;
            }
            ++reservations;
        }
        queue.add(msg);
    }

    public void shutdown() {
        synchronized (this) {
            isShutdown = true;
        }
        loggerThread.interrupt();
    }

    private class LoggerThread extends Thread {
        @Override
        public void run() {
            try {
                while (true) {
                    try {
                        synchronized (LogService.this) {
                            // reservations works like a flush on
                            // on a non-empty queue when shutdown
                            // is requested
                            if (isShutdown && reservations == 0) {
                                break;
                            }
                        }

                        // avoid adding blocking take() in a synchronized block
                        String msg = LogService.this.queue.take();

                        synchronized (LogService.this) {
                            --reservations;
                        }

                        // use the writer to log 'msg'

                    } catch (InterruptedException e) {
                        // interrupt won't prevent messages which
                        // were submitted to the queue before
                        // interrupt from being logged
                    }
                }
            } finally {
                // close the writer
            }
        }
    }
}
