package ch8_applying_thread_pools.extending_thread_pool_executor;

import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class ThreadPoolWithLoggingAndTiming {}

class TimingThreadPool extends ThreadPoolExecutor {

    private final ThreadLocal<Instant> startTime = new ThreadLocal<>();
    private final Logger log = Logger.getLogger("TimingThreadPool");
    private final AtomicLong numTasks = new AtomicLong(0);
    private final AtomicLong totalDuration = new AtomicLong();

    public TimingThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        numTasks.incrementAndGet();
        startTime.set(Instant.now());
    }

    // execution hooks are called by the thread that executes
    // the task, hence afterExecute can access startTime which is local
    // to a thread
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        try {
            totalDuration.addAndGet(Instant.now().getEpochSecond() - startTime.get().getEpochSecond());
        } finally {
            super.afterExecute(r, t);
        }
    }

    @Override
    protected void terminated() {
        try {
            // log some statistics here
        } finally {
            super.terminated();
        }
    }
}
