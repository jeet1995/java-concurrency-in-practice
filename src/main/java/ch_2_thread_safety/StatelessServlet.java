package ch_2_thread_safety;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates a thread-safe stateless servlet.
 * A class is thread-safe if it behaves correctly when accessed from multiple threads,
 * regardless of the scheduling or interleaving of the execution of those threads by the runtime environment,
 * and with no additional synchronization or coordination on the part of the calling code.
 * 
 * This servlet is thread-safe because it has no state (no instance variables) and only uses
 * local variables and parameters. Each thread gets its own stack frame with its own copy
 * of local variables, so there's no shared mutable state that could cause thread safety issues.
 */
public class StatelessServlet {
    private static final Logger logger = LogManager.getLogger(StatelessServlet.class);

    /**
     * Processes a request by factoring a number.
     * This method is thread-safe because:
     * 1. It has no instance variables (no state)
     * 2. All variables used are local to the method
     * 3. Parameters are passed by value
     * 4. It doesn't access any shared mutable state
     */
    public void service(String request, Response response) {
        // Extract the number to factor from the request
        BigInteger number = extractFromRequest(request);
        
        // Factor the number (simulated with a delay)
        BigInteger[] factors = factor(number);
        
        // Encode the response
        encodeIntoResponse(response, factors);
    }

    /**
     * Extracts the number to factor from the request.
     * This method is thread-safe because it only uses its parameters and local variables.
     */
    private BigInteger extractFromRequest(String request) {
        logger.debug("Extracting number from request: {}", request);
        // Simulate request parsing
        try {
            TimeUnit.MILLISECONDS.sleep(100); // Simulate some work
        } catch (InterruptedException e) {
            logger.error("Request processing interrupted", e);
            Thread.currentThread().interrupt();
        }
        return new BigInteger(request);
    }

    /**
     * Factors the given number.
     * This method is thread-safe because it only uses its parameters and local variables.
     */
    private BigInteger[] factor(BigInteger number) {
        logger.debug("Factoring number: {}", number);
        // Simulate factoring operation
        try {
            TimeUnit.MILLISECONDS.sleep(200); // Simulate some work
        } catch (InterruptedException e) {
            logger.error("Factoring interrupted", e);
            Thread.currentThread().interrupt();
        }
        // Return dummy factors for demonstration
        return new BigInteger[]{BigInteger.ONE, number};
    }

    /**
     * Encodes the factors into the response.
     * This method is thread-safe because it only uses its parameters and local variables.
     */
    private void encodeIntoResponse(Response response, BigInteger[] factors) {
        logger.debug("Encoding response with factors: {}", factors);
        // Simulate response encoding
        try {
            TimeUnit.MILLISECONDS.sleep(100); // Simulate some work
        } catch (InterruptedException e) {
            logger.error("Response encoding interrupted", e);
            Thread.currentThread().interrupt();
        }
        response.setFactors(factors);
    }

    /**
     * Demonstrates the thread-safety of the stateless servlet
     */
    public static void main(String[] args) throws InterruptedException {
        StatelessServlet servlet = new StatelessServlet();
        
        // Create multiple threads to demonstrate concurrent access
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                String request = "42"; // Example request
                Response response = new Response();
                servlet.service(request, response);
                logger.info("Thread {} processed request: {}", threadId, response);
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }
    }

    /**
     * Simple response class to hold the result
     */
    private static class Response {
        private BigInteger[] factors;

        public void setFactors(BigInteger[] factors) {
            this.factors = factors;
        }

        @Override
        public String toString() {
            return "Response{factors=" + (factors != null ? factors[0] + ", " + factors[1] : "null") + '}';
        }
    }
} 