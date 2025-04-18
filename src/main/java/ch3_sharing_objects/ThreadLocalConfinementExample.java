package ch3_sharing_objects;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates safe publication using ThreadLocal for thread confinement.
 * ThreadLocal provides a way to associate a value with a thread, ensuring
 * that each thread has its own copy of the value.
 */
public class ThreadLocalConfinementExample {
    
    /**
     * A mutable object that is not thread-safe.
     */
    public static class MutableObject {
        private int value;
        
        public MutableObject(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
        
        public void setValue(int value) {
            this.value = value;
        }
        
        @Override
        public String toString() {
            return "MutableObject{value=" + value + "}";
        }
    }
    
    /**
     * A thread-safe wrapper that uses ThreadLocal to confine a mutable object to a single thread.
     * Each thread gets its own copy of the object, eliminating the need for synchronization.
     * Implements AutoCloseable to support proper resource cleanup.
     */
    public static class ThreadLocalConfinedObject implements AutoCloseable {
        // ThreadLocal to store the mutable object for each thread
        private final ThreadLocal<MutableObject> threadLocalObject = new ThreadLocal<>();
        
        /**
         * Initializes the ThreadLocal with a new MutableObject for the current thread.
         * @param initialValue The initial value for the MutableObject
         */
        public void initialize(int initialValue) {
            threadLocalObject.set(new MutableObject(initialValue));
        }
        
        /**
         * Gets the value from the ThreadLocal MutableObject.
         * @return The current value
         * @throws IllegalStateException if the ThreadLocal is not initialized
         */
        public int getValue() {
            MutableObject obj = threadLocalObject.get();
            if (obj == null) {
                throw new IllegalStateException("ThreadLocal not initialized for thread " + 
                    Thread.currentThread().getName());
            }
            return obj.getValue();
        }
        
        /**
         * Sets the value in the ThreadLocal MutableObject.
         * @param value The new value
         * @throws IllegalStateException if the ThreadLocal is not initialized
         */
        public void setValue(int value) {
            MutableObject obj = threadLocalObject.get();
            if (obj == null) {
                throw new IllegalStateException("ThreadLocal not initialized for thread " + 
                    Thread.currentThread().getName());
            }
            obj.setValue(value);
        }
        
        /**
         * Creates a copy of the current thread's MutableObject that can be safely shared.
         * This is a safe publication mechanism.
         * @return A copy of the MutableObject
         * @throws IllegalStateException if the ThreadLocal is not initialized
         */
        public MutableObject getCopy() {
            MutableObject obj = threadLocalObject.get();
            if (obj == null) {
                throw new IllegalStateException("ThreadLocal not initialized for thread " + 
                    Thread.currentThread().getName());
            }
            return new MutableObject(obj.getValue());
        }
        
        /**
         * Removes the ThreadLocal value for the current thread.
         * This is important to prevent memory leaks in thread pools.
         * This method is called automatically when used in a try-with-resources statement.
         */
        @Override
        public void close() {
            threadLocalObject.remove();
        }
        
        @Override
        public String toString() {
            MutableObject obj = threadLocalObject.get();
            if (obj == null) {
                return "ThreadLocalConfinedObject{not initialized}";
            }
            return obj.toString();
        }
    }
    
    /**
     * Demonstrates thread confinement using ThreadLocal and safe publication.
     */
    public static void main(String[] args) throws InterruptedException {
        // Create a thread-confined object using ThreadLocal
        ThreadLocalConfinedObject confined = new ThreadLocalConfinedObject();
        
        // Initialize it for the main thread
        confined.initialize(42);
        
        // Access from the main thread
        System.out.println("Accessing from main thread: " + confined.getValue());
        confined.setValue(100);
        System.out.println("Updated value: " + confined.getValue());
        
        // Create a thread pool
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        // Task 1: Initialize and access the ThreadLocal in thread 1
        executor.submit(() -> {
            try {
                System.out.println("Thread 1 initializing ThreadLocal...");
                confined.initialize(200);
                System.out.println("Thread 1 accessing ThreadLocal: " + confined.getValue());
                confined.setValue(300);
                System.out.println("Thread 1 updated value: " + confined.getValue());
            } catch (Exception e) {
                System.out.println("Thread 1 caught exception: " + e.getMessage());
            } finally {
                // Clean up to prevent memory leaks
                confined.close();
            }
            return null;
        });
        
        // Task 2: Initialize and access the ThreadLocal in thread 2
        executor.submit(() -> {
            try {
                System.out.println("Thread 2 initializing ThreadLocal...");
                confined.initialize(400);
                System.out.println("Thread 2 accessing ThreadLocal: " + confined.getValue());
                confined.setValue(500);
                System.out.println("Thread 2 updated value: " + confined.getValue());
            } catch (Exception e) {
                System.out.println("Thread 2 caught exception: " + e.getMessage());
            } finally {
                // Clean up to prevent memory leaks
                confined.close();
            }
            return null;
        });
        
        // Shutdown executor and wait for tasks to complete
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
        
        // Main thread's value should still be 100
        System.out.println("\nMain thread value after other threads: " + confined.getValue());
        
        // Demonstrate safe publication
        System.out.println("\nDemonstrating safe publication:");
        
        // Create a list to store published copies
        List<MutableObject> publishedObjects = new ArrayList<>();
        
        // Publish copies from the main thread
        publishedObjects.add(confined.getCopy());
        confined.setValue(150);
        publishedObjects.add(confined.getCopy());
        
        // Access the published copies from any thread
        executor = Executors.newFixedThreadPool(2);
        
        executor.submit(() -> {
            System.out.println("Thread 1 accessing published object: " + publishedObjects.get(0));
            return null;
        });
        
        executor.submit(() -> {
            System.out.println("Thread 2 accessing published object: " + publishedObjects.get(1));
            return null;
        });
        
        // Shutdown executor
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
        
        // Demonstrate try-with-resources pattern
        System.out.println("\nDemonstrating try-with-resources pattern:");
        try (ThreadLocalConfinedObject autoClosed = new ThreadLocalConfinedObject()) {
            autoClosed.initialize(999);
            System.out.println("Value in try-with-resources: " + autoClosed.getValue());
        } // close() is called automatically here
        
        // Clean up the main thread's ThreadLocal
        confined.close();
        
        // Print summary
        System.out.println("\nSummary:");
        System.out.println("1. ThreadLocal provides automatic thread confinement");
        System.out.println("2. Each thread gets its own copy of the object");
        System.out.println("3. Safe publication can be achieved by creating copies");
        System.out.println("4. Implementing AutoCloseable allows for proper resource cleanup");
        System.out.println("5. Try-with-resources ensures cleanup even if exceptions occur");
    }
} 