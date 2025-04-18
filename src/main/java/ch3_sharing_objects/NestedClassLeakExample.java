package ch3_sharing_objects;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates how non-static nested classes can leak references to their enclosing instance
 * and how to prevent such leaks using static nested classes.
 */
public class NestedClassLeakExample {
    private final String secretData = "Sensitive information";
    private static final String STATIC_DATA = "Static information";

    /**
     * Non-static nested class that implicitly holds a reference to the enclosing instance.
     * This can lead to memory leaks and unexpected behavior.
     */
    public class InnerClass {
        public void accessEnclosing() {
            // Can access both static and non-static members of enclosing class
            System.out.println("Accessing from inner class: " + secretData);
            System.out.println("Accessing static data: " + STATIC_DATA);
        }

        public NestedClassLeakExample getEnclosingInstance() {
            // Can return reference to enclosing instance
            return NestedClassLeakExample.this;
        }
    }

    /**
     * Static nested class that doesn't hold a reference to the enclosing instance.
     * This is the preferred way to create nested classes when you don't need
     * access to the enclosing instance.
     */
    public static class StaticNestedClass {
        public void accessStatic() {
            // Can only access static members of enclosing class
            System.out.println("Accessing static data: " + STATIC_DATA);
        }
    }

    /**
     * Demonstrates the leak and proper usage of nested classes.
     */
    public static void main(String[] args) throws InterruptedException {
        NestedClassLeakExample outer = new NestedClassLeakExample();
        
        // Create inner class instance - holds reference to outer
        InnerClass inner = outer.new InnerClass();
        
        // Create static nested class instance - doesn't hold reference to outer
        StaticNestedClass staticInner = new StaticNestedClass();

        // Demonstrate the leak
        System.out.println("Demonstrating inner class leak:");
        NestedClassLeakExample leakedReference = inner.getEnclosingInstance();
        System.out.println("Got reference to outer instance: " + (leakedReference == outer));

        // Demonstrate proper usage with static nested class
        System.out.println("\nDemonstrating static nested class (no leak):");
        staticInner.accessStatic();

        // Demonstrate memory implications with ExecutorService
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        // Submit task using inner class - holds reference to outer
        executor.submit(() -> {
            inner.accessEnclosing();
            return null;
        });

        // Submit task using static nested class - doesn't hold reference to outer
        executor.submit(() -> {
            staticInner.accessStatic();
            return null;
        });

        // Shutdown executor
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);

        // At this point, even though 'outer' is no longer used,
        // the inner class instance still holds a reference to it
        // This could prevent garbage collection of the outer instance
        // if the inner instance is still reachable
    }
} 