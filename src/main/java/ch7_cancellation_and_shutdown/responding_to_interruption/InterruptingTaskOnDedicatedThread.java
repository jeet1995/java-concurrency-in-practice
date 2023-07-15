package ch7_cancellation_and_shutdown.responding_to_interruption;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// Ex 7.9
public class InterruptingTaskOnDedicatedThread {

    private static final ScheduledExecutorService cancellableExecutor = Executors.newScheduledThreadPool(4);

    public void timedRun(Runnable r, long timeout, TimeUnit timeUnit) throws Throwable {

        class RethrowableTask implements Runnable {

            // allows for safe publishing of throwable
            // instance between thread running "r" and thread
            // running "rethrowableTask"
            volatile Throwable throwable;

            @Override
            public void run() {
                try {
                    r.run();
                } catch (Exception e) {
                    this.throwable = e;
                }
            }

            public void rethrow() throws Throwable {
                throw throwable;
            }
        }

        final RethrowableTask rethrowableTask = new RethrowableTask();
        Thread threadForRethrowableTask = new Thread(rethrowableTask);
        threadForRethrowableTask.start();
        cancellableExecutor.schedule(new Runnable() {
            @Override
            public void run() {
                // interruption is ok here since threadForRethrowableTask
                // is managed in this scope
                threadForRethrowableTask.interrupt();
            }
        }, timeout, timeUnit);
        // the calling thread will wait for "timeout" time
        threadForRethrowableTask.join(timeUnit.toMillis(timeout));
        // use the throwable instance from the original task and rethrow
        // on a dedicated thread so the caller can handle the exception
        rethrowableTask.rethrow();
    }

}
