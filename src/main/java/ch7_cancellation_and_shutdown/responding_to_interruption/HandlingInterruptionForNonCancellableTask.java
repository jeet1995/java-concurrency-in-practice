package ch7_cancellation_and_shutdown.responding_to_interruption;

import java.util.concurrent.BlockingQueue;

public class HandlingInterruptionForNonCancellableTask {

    // Q: Does getNextTask and tasks.take() execute on different threads?
    //      I ask this Q since getNextTask doesn't implicitly get interrupted when tasks.take() is interrupted
    public NonCancellableTask getNextTask(BlockingQueue<NonCancellableTask> tasks) {
        boolean interrupted = false;

        try {
            while (true) {
                try {
                    // if tasks.take() were to be uninterruptible then
                    // the try-block would have to be enclosed with while(!Thread.currentThread().isInterrupted())
                    return tasks.take();
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

class NonCancellableTask implements Runnable {
    @Override
    public void run() {
        System.out.println("Do something!");
    }
}