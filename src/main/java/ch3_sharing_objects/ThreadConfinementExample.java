package ch3_sharing_objects;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates safe publication using thread confinement.
 * Thread confinement ensures that an object is only accessed by a single thread,
 * eliminating the need for synchronization.
 */
public class ThreadConfinementExample {
    
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
     * A thread-safe wrapper that confines a mutable object to a single thread.
     * The object is only accessible through methods that execute on the confined thread.
     */
    public static class ThreadConfinedObject {
        private final Thread confinedThread;
        private final MutableObject object;
        
        public ThreadConfinedObject(MutableObject object) {
            this.object = object;
            this.confinedThread = Thread.currentThread();
        }
        
        /**
         * Checks if the current thread is the confined thread.
         * @throws IllegalStateException if accessed from a different thread
         */
        private void checkThread() {
            if (Thread.currentThread() != confinedThread) {
                throw new IllegalStateException(
                    "Object confined to thread " + confinedThread.getName() + 
                    ", accessed from thread " + Thread.currentThread().getName());
            }
        }
        
        public int getValue() {
            checkThread();
            return object.getValue();
        }
        
        public void setValue(int value) {
            checkThread();
            object.setValue(value);
        }
        
        /**
         * Creates a copy of the object that can be safely shared with other threads.
         * This is a safe publication mechanism.
         */
        public MutableObject getCopy() {
            checkThread();
            return new MutableObject(object.getValue());
        }
        
        @Override
        public String toString() {
            checkThread();
            return object.toString();
        }
    }
    
    /**
     * Demonstrates thread confinement and safe publication.
     */
    public static void main(String[] args) throws InterruptedException {
        // Create a mutable object
        MutableObject original = new MutableObject(42);
        
        // Confine it to the current thread
        ThreadConfinedObject confined = new ThreadConfinedObject(original);
        
        // Access from the confined thread (main thread)
        System.out.println("Accessing from confined thread: " + confined.getValue());
        confined.setValue(100);
        System.out.println("Updated value: " + confined.getValue());
        
        // Create a thread pool
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        // Try to access from different threads
        List<Exception> exceptions = new ArrayList<>();
        
        // Task 1: Try to access the confined object directly
        executor.submit(() -> {
            try {
                System.out.println("Thread 1 trying to access confined object...");
                confined.getValue(); // This should throw an exception
            } catch (Exception e) {
                exceptions.add(e);
                System.out.println("Thread 1 caught exception: " + e.getMessage());
            }
            return null;
        });
        
        // Task 2: Get a copy and modify it
        executor.submit(() -> {
            try {
                System.out.println("Thread 2 getting a copy of the confined object...");
                MutableObject copy = confined.getCopy(); // This should throw an exception
                System.out.println("Thread 2 got copy: " + copy);
                copy.setValue(200);
                System.out.println("Thread 2 modified copy: " + copy);
            } catch (Exception e) {
                exceptions.add(e);
                System.out.println("Thread 2 caught exception: " + e.getMessage());
            }
            return null;
        });
        
        // Shutdown executor and wait for tasks to complete
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
        
        // Demonstrate safe publication
        System.out.println("\nDemonstrating safe publication:");
        
        // Create a new thread-confined object
        ThreadConfinedObject confined2 = new ThreadConfinedObject(new MutableObject(42));
        
        // Create a thread-safe list for publishing copies
        List<MutableObject> publishedObjects = new ArrayList<>();
        
        // Publish copies from the confined thread
        publishedObjects.add(confined2.getCopy());
        confined2.setValue(100);
        publishedObjects.add(confined2.getCopy());
        
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
        
        // Print summary
        System.out.println("\nSummary:");
        System.out.println("1. Thread confinement prevents access from other threads");
        System.out.println("2. Safe publication can be achieved by creating copies");
        System.out.println("3. Published copies can be accessed from any thread");
    }
} 