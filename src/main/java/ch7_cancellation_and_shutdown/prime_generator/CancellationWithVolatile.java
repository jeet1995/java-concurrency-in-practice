package ch7_cancellation_and_shutdown.prime_generator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Ex 7.1: Java concurrency in practice
public class CancellationWithVolatile {
    public static void main(String[] args) {
        PrimeGenerator primeGenerator = new PrimeGenerator();

        Thread thread = new Thread(primeGenerator);
        thread.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println("InterruptedException thrown...");
        } finally {
            primeGenerator.cancel();
        }

        List<BigInteger> allPrimesGeneratedInASecond = primeGenerator.get();
    }
}

class PrimeGenerator implements Runnable {
    private List<BigInteger> primes = new ArrayList<>();
    private volatile boolean isCancelled = false;

    @Override
    public void run() {

        BigInteger nextProbablePrime = BigInteger.ONE;

        while (!isCancelled) {
            nextProbablePrime = nextProbablePrime.nextProbablePrime();
            synchronized (this) {
                primes.add(nextProbablePrime);
            }
        }
    }

    public void cancel() {
        this.isCancelled = true;
    }

    public synchronized List<BigInteger> get() {
        return Collections.unmodifiableList(primes);
    }
}


