# Chapter 3: Sharing Objects

This package contains examples demonstrating thread safety concepts related to sharing objects between threads, as discussed in Chapter 3 of "Java Concurrency in Practice."

## Key Concepts Demonstrated

### 1. Nested Class Reference Leaks (`NestedClassLeakExample.java`)

This example demonstrates:
- How non-static nested classes implicitly hold references to their enclosing instance
- Potential memory leaks when using non-static nested classes in long-lived contexts
- Solution using static nested classes to prevent enclosing instance reference leaks
- Best practices for nested class design in concurrent applications

Key takeaways:
- Non-static nested classes hold an implicit reference to their enclosing instance
- Static nested classes don't hold references to the enclosing instance
- Use static nested classes when the nested class doesn't need access to enclosing instance members
- Be cautious when using non-static nested classes in thread pools or other long-lived contexts

### 2. Thread Confinement (`ThreadLocalConfinementExample.java`)

This example demonstrates:
- Using `ThreadLocal` for thread confinement
- Safe publication of confined objects
- Proper resource management with `AutoCloseable`
- Prevention of memory leaks in thread pools

Key features:
- Thread-local storage of mutable objects
- Safe initialization and cleanup of thread-local values
- Copy-based publication mechanism
- Integration with try-with-resources for automatic cleanup

## Best Practices

1. **Thread Safety Through Confinement**
   - Use thread confinement when synchronization is expensive or unnecessary
   - Ensure proper cleanup of thread-local resources
   - Use `AutoCloseable` for systematic resource management

2. **Safe Publication**
   - Create defensive copies when sharing confined objects
   - Use immutable objects when possible
   - Be careful with object references in concurrent contexts

3. **Resource Management**
   - Implement `AutoCloseable` for resources requiring cleanup
   - Use try-with-resources for automatic cleanup
   - Always clean up thread-local values in thread pools

## Example Usage

### Thread Confinement Example
```java
try (ThreadLocalConfinedObject confined = new ThreadLocalConfinedObject()) {
    confined.initialize(42);
    // Each thread gets its own copy of the value
    int value = confined.getValue();
    // Modifications are thread-safe
    confined.setValue(100);
    // Create a safe copy for sharing
    MutableObject copy = confined.getCopy();
} // Automatic cleanup through AutoCloseable
```

### Static Nested Class Example
```java
// Prefer static nested classes for thread safety
public static class StaticEventListener implements EventListener {
    // No implicit reference to enclosing instance
    public void onEvent(String event) {
        // Handle event
    }
}
```

## Further Reading

- Chapter 3 of "Java Concurrency in Practice"
- Java Documentation on [ThreadLocal](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/ThreadLocal.html)
- Java Documentation on [Nested Classes](https://docs.oracle.com/javase/tutorial/java/javaOO/nested.html)
- Java Documentation on [try-with-resources](https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html) 