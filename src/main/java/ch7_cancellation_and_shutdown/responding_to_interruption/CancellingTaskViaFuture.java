package ch7_cancellation_and_shutdown.responding_to_interruption;

import java.util.concurrent.*;

// Ex 7.10
public class CancellingTaskViaFuture {
    private static final ScheduledExecutorService cancellableExecutor = Executors.newScheduledThreadPool(4);

    public void timedRun(Runnable r, long timeout, TimeUnit timeUnit) throws Throwable {

        // Future is a recommended way of managing the lifecycle
        // of a task
        Future<?> taskFuture = cancellableExecutor.submit(r);

        try {
            taskFuture.get(timeout, TimeUnit.SECONDS);
        } catch (TimeoutException timeoutException) {
            // handle timeout through interruption
        } catch (ExecutionException executionException) {
            // throw exception back to caller after laundering it
        } finally {
            // 1. This will interrupt a thread that is
            //    executing the task.
            // 2. This will also stop a thread from executed
            //    which hasn't started executing already.
            taskFuture.cancel(true);
        }
    }
}
