package ch_1_introduction;

// Import AtomicInteger for atomic operations
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Listing 1.2 from Java Concurrency in Practice
 * Demonstrates different approaches to thread-safe sequence generation:
 * 1. Method-level synchronization (check-then-act pattern)
 * 2. Class-level synchronization
 * 3. Atomic operations (preferred approach)
 */
public class ThreadSafeSequenceGenerator {
    // Log4j2 logger instance for this class
    private static final Logger logger = LogManager.getLogger(ThreadSafeSequenceGenerator.class);

    // Regular integer field that will be accessed by multiple threads
    // This field is not thread-safe by itself
    private int value;
    
    // AtomicInteger provides thread-safe operations on an integer value
    // This is the preferred way to handle integer operations in concurrent scenarios
    private final AtomicInteger atomicValue = new AtomicInteger(0);

    /**
     * Even though this method is synchronized, it demonstrates a "check-then-act" pattern
     * which can be problematic in concurrent scenarios. The increment operation (value++)
     * is actually three operations:
     * 1. Read the current value
     * 2. Increment it
     * 3. Write the new value back
     * 
     * While synchronization prevents multiple threads from executing this method simultaneously,
     * it's better to use atomic operations (like AtomicInteger) for such cases to make the
     * intent clearer and potentially more efficient.
     */
    // synchronized keyword ensures only one thread can execute this method at a time
    // This is method-level synchronization, locking on the instance object (this)
    public synchronized int getNext() {
        return value++; // Compound operation: read, increment, write
    }
    
    /**
     * Improved version that uses AtomicInteger to avoid the check-then-act pattern.
     * AtomicInteger provides atomic operations that combine the read, modify, and write
     * operations into a single atomic operation. This is more efficient and clearer in intent
     * than using synchronization.
     */
    // getAndIncrement() performs the read, increment, and write as a single atomic operation
    // This is more efficient than synchronized method as it uses hardware-level atomic operations
    public int getNextImprovement() {
        return atomicValue.getAndIncrement();
    }
    
    // Demonstrates class-level synchronization
    // This is more restrictive than instance-level synchronization as it locks on the class object
    // Only one thread can access any instance of the class at a time
    public int getNextWithClassLock() {
        synchronized(ThreadSafeSequenceGenerator.class) {
            return value++;
        }
    }
    
    // Demonstrates static method synchronization
    // This is equivalent to synchronized(ThreadSafeSequenceGenerator.class)
    // Cannot access instance variables (value) as it's a static method
    public static synchronized int getNextStatic() {
        // Note: This won't work because static methods can't access instance variables
        // return value++; // This would cause a compilation error
        return 0; // Placeholder
    }

    /**
     * Demonstrates method-level synchronization using synchronized keyword
     * Creates multiple threads to show concurrent access to the synchronized method
     */
    private static void demonstrateMethodLevelSynchronization() throws InterruptedException {
        logger.info("Demonstrating synchronized getNext():");
        final ThreadSafeSequenceGenerator syncGenerator = new ThreadSafeSequenceGenerator();
        Thread[] threads = createAndStartThreads(syncGenerator::getNext);
        waitForThreads(threads);
    }

    /**
     * Demonstrates atomic operations using AtomicInteger
     * Creates multiple threads to show concurrent access to atomic operations
     */
    private static void demonstrateAtomicOperations() throws InterruptedException {
        logger.info("Demonstrating atomic getNextImprovement():");
        final ThreadSafeSequenceGenerator atomicGenerator = new ThreadSafeSequenceGenerator();
        Thread[] threads = createAndStartThreads(atomicGenerator::getNextImprovement);
        waitForThreads(threads);
    }

    /**
     * Demonstrates static method synchronization
     * Creates multiple threads to show concurrent access to static synchronized method
     */
    private static void demonstrateStaticSynchronization() throws InterruptedException {
        logger.info("Demonstrating static synchronized getNextStatic():");
        Thread[] threads = createAndStartThreads(() -> ThreadSafeSequenceGenerator.getNextStatic());
        waitForThreads(threads);
    }

    /**
     * Creates and starts multiple threads that will call the provided sequence generator
     * @param sequenceGenerator The function to call for getting the next sequence number
     * @return Array of started threads
     */
    private static Thread[] createAndStartThreads(SequenceGeneratorFunction sequenceGenerator) {
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 3; j++) {
                    logger.debug("Thread {} got value: {}", threadId, sequenceGenerator.getNext());
                }
            });
            threads[i].start();
        }
        return threads;
    }

    /**
     * Waits for all threads to complete using join()
     * @param threads Array of threads to wait for
     * @throws InterruptedException if any thread is interrupted while waiting
     */
    private static void waitForThreads(Thread[] threads) throws InterruptedException {
        // thread.join() is crucial here for thread coordination:
        // 1. It makes the main thread wait until the target thread completes
        // 2. Without join(), the main thread might continue before all threads finish
        // 3. This ensures we see all output before starting the next demonstration
        // 4. The InterruptedException is thrown if the thread is interrupted while waiting
        for (Thread thread : threads) {
            thread.join();
        }
    }

    /**
     * Functional interface for sequence generator methods
     */
    @FunctionalInterface
    private interface SequenceGeneratorFunction {
        int getNext();
    }

    /**
     * Main method demonstrating the usage of different synchronization approaches
     * Creates multiple threads to show concurrent access to the sequence generator
     */
    public static void main(String[] args) throws InterruptedException {
        demonstrateMethodLevelSynchronization();
        demonstrateAtomicOperations();
        demonstrateStaticSynchronization();
    }
} 