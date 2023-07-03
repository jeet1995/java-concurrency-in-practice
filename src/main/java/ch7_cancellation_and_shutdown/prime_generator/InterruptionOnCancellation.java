package ch7_cancellation_and_shutdown.prime_generator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InterruptionOnCancellation {
}

class PrimeGeneratorWithInterrupt extends Thread {
    private List<BigInteger> primes = new ArrayList<>();
    private volatile boolean isCancelled = false;

    @Override
    public void run() {

        BigInteger nextProbablePrime = BigInteger.ONE;

        // check / poll the interrupted status of the current thread
        while (!Thread.currentThread().isInterrupted()) {
            nextProbablePrime = nextProbablePrime.nextProbablePrime();
            primes.add(nextProbablePrime);
        }
    }

    // use interrupt as a way to cancel the task
    public void cancel() {
        interrupt();
    }

    public synchronized List<BigInteger> get() {
        return Collections.unmodifiableList(primes);
    }
}
