package ch7_cancellation_and_shutdown.responding_to_interruption;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SchedulingInterruptOnBorrowedThread {

    private static final ScheduledExecutorService cancellableExecutor = Executors.newScheduledThreadPool(4);

    public void timedRun(Runnable r, long timeout, TimeUnit timeUnit) {
        final Thread threadForTask = Thread.currentThread();
        // interrupt after "timeout" delay
        // bad practice since we do not know / manage the
        // interruption policy for "threadForTask" in this scope
        //  1. interruption could be called when the task is finished and a new task
        //     is executing
        //  2. may not be responsive to interruption
        cancellableExecutor.schedule(threadForTask::interrupt, timeout, timeUnit);
        r.run();
    }
}
