package ch7_cancellation_and_shutdown.prime_generator;

import java.math.BigInteger;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

// Ex 7.5: Java concurrency in practice
public class CancellationOnATaskOnABlockingThread {

    public static void main(String[] args) {
        BlockingQueue<BigInteger> primes = new ArrayBlockingQueue<>(1000);
        BrokenPrimeGenerator brokenPrimeGenerator = new BrokenPrimeGenerator(primes);

        Thread thread = new Thread(brokenPrimeGenerator);
        thread.start();
        long start = System.currentTimeMillis();

        try {
            while (needMorePrimes(start, 1000)) {
                consume(primes.take());
            }
        } catch (InterruptedException e) {

        } finally {
            brokenPrimeGenerator.cancel();
        }
    }

    static void consume(BigInteger prime) {
        // do something with the consumed prime
    }

    static boolean needMorePrimes(long startTime, long maxTime) {
        return System.currentTimeMillis() - startTime <= maxTime;
    }
}

class BrokenPrimeGenerator implements Runnable {
    private final BlockingQueue<BigInteger> primes;
    private volatile boolean isCancelled;

    public BrokenPrimeGenerator(BlockingQueue<BigInteger> primes) {
        this.primes = primes;
        this.isCancelled = false;
    }

    @Override
    public void run() {
        BigInteger nextPrime = BigInteger.ONE;
        while (!isCancelled) {
            nextPrime = nextPrime.nextProbablePrime();
            // this is a blocking operation when the queue is full
            // therefore the thread won't exit this loop to
            // check the cancellation status
            // a better approach is to bind the cancellation status of the task
            // to the interruption status of the thread
            primes.add(nextPrime);
        }
    }

    public void cancel() {
        this.isCancelled = true;
    }
}
