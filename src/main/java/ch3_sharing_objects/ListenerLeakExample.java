package ch3_sharing_objects;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Demonstrates how registering a listener can leak references to the enclosing class
 * and how to prevent such leaks using static nested classes or lambda expressions.
 */
public class ListenerLeakExample {
    private final String secretData = "Sensitive information";
    private final List<EventListener> listeners = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * Interface for event listeners.
     */
    public interface EventListener {
        void onEvent(String event);
    }

    /**
     * Non-static nested class that implicitly holds a reference to the enclosing instance.
     * When registered as a listener, it can cause memory leaks.
     */
    public class InnerEventListener implements EventListener {
        @Override
        public void onEvent(String event) {
            // Can access both static and non-static members of enclosing class
            System.out.println("Inner listener received event: " + event);
            System.out.println("Can access secret data: " + secretData);
        }
    }

    /**
     * Static nested class that doesn't hold a reference to the enclosing instance.
     * This is safer for listener registration.
     */
    public static class StaticEventListener implements EventListener {
        private final String data;

        public StaticEventListener(String data) {
            this.data = data;
        }

        @Override
        public void onEvent(String event) {
            System.out.println("Static listener received event: " + event);
            System.out.println("Using provided data: " + data);
        }
    }

    /**
     * Registers a listener that can leak the enclosing instance.
     */
    public void registerInnerListener() {
        // This creates an inner class instance that holds a reference to 'this'
        InnerEventListener listener = new InnerEventListener();
        listeners.add(listener);
        System.out.println("Registered inner listener");
    }

    /**
     * Registers a listener that doesn't leak the enclosing instance.
     */
    public void registerStaticListener() {
        // This creates a static nested class instance that doesn't hold a reference to 'this'
        StaticEventListener listener = new StaticEventListener("Static data");
        listeners.add(listener);
        System.out.println("Registered static listener");
    }

    /**
     * Registers a listener using a lambda expression.
     * Lambda expressions don't capture the enclosing instance unless they reference it.
     */
    public void registerLambdaListener() {
        // This lambda doesn't reference the enclosing instance, so it's safe
        listeners.add(event -> System.out.println("Lambda listener received event: " + event));
        System.out.println("Registered lambda listener");
    }

    /**
     * Registers a listener using a lambda expression that does reference the enclosing instance.
     * This will still leak the reference.
     */
    public void registerLeakingLambdaListener() {
        // This lambda references the enclosing instance, causing a leak
        listeners.add(event -> {
            System.out.println("Leaking lambda listener received event: " + event);
            System.out.println("Can access secret data: " + secretData);
        });
        System.out.println("Registered leaking lambda listener");
    }

    /**
     * Fires an event to all registered listeners.
     */
    public void fireEvent(String event) {
        for (EventListener listener : listeners) {
            listener.onEvent(event);
        }
    }

    /**
     * Demonstrates the listener leak and proper usage of listeners.
     */
    public static void main(String[] args) throws InterruptedException {
        ListenerLeakExample example = new ListenerLeakExample();
        
        // Register different types of listeners
        example.registerInnerListener();
        example.registerStaticListener();
        example.registerLambdaListener();
        example.registerLeakingLambdaListener();
        
        // Fire an event
        example.fireEvent("Test event");
        
        // Demonstrate memory implications
        System.out.println("\nDemonstrating memory implications:");
        
        // Create a new executor for long-running tasks
        ExecutorService longRunningExecutor = Executors.newSingleThreadExecutor();
        
        // Submit a task that keeps a reference to the inner listener
        longRunningExecutor.submit(() -> {
            // This task holds a reference to the inner listener
            // which in turn holds a reference to the enclosing instance
            try {
                Thread.sleep(1000);
                System.out.println("Long-running task completed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return null;
        });
        
        // Shutdown executors
        example.executor.shutdown();
        longRunningExecutor.shutdown();
        example.executor.awaitTermination(1, TimeUnit.SECONDS);
        longRunningExecutor.awaitTermination(1, TimeUnit.SECONDS);
        
        // At this point, even though 'example' is no longer used in the main method,
        // the inner listener and leaking lambda still hold references to it
        // This could prevent garbage collection of the 'example' instance
        // if these listeners are still reachable
    }
} 