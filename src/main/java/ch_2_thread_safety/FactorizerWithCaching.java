package ch_2_thread_safety;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class demonstrates thread safety issues with caching.
 * It shows how adding state (caching) to a previously thread-safe class
 * can introduce thread safety problems and how to handle them properly.
 * 
 * Thread Safety Considerations:
 * 1. Uses ConcurrentHashMap for thread-safe map operations
 * 2. Uses AtomicLong for thread-safe counter operations
 * 3. NumberFactorization is immutable, making it inherently thread-safe
 * 4. All state is final to prevent reassignment
 */
public class FactorizerWithCaching {
    // Thread-safe map implementation for caching factorizations
    // ConcurrentHashMap provides atomic operations and better scalability
    private final ConcurrentHashMap<BigInteger, NumberFactorization> cache = new ConcurrentHashMap<>();
    
    // Thread-safe counters using AtomicLong
    // AtomicLong provides atomic increment and get operations
    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);
    
    // Immutable default instance shared across all threads
    // Safe because NumberFactorization is immutable
    private static final NumberFactorization DEFAULT_FACTORIZATION = 
        new NumberFactorization(BigInteger.ZERO, new ArrayList<>());

    /**
     * Gets the cached factorization if available.
     * Thread-safe because:
     * 1. ConcurrentHashMap.get() is atomic
     * 2. AtomicLong.incrementAndGet() is atomic
     * 3. Returns immutable NumberFactorization objects
     * 
     * @param number The number to factor
     * @return The cached factorization if available, or a default instance if not found
     */
    public NumberFactorization getCachedFactorization(BigInteger number) {
        NumberFactorization cached = this.cache.get(number);
        if (cached != null) {
            this.hits.incrementAndGet();
            return cached;
        }
        this.misses.incrementAndGet();
        return DEFAULT_FACTORIZATION;
    }

    /**
     * Updates the cache with a new factorization.
     * Thread-safe because:
     * 1. ConcurrentHashMap.put() is atomic
     * 2. NumberFactorization is immutable
     * 
     * @param factorization The new factorization to cache
     */
    public void updateCache(NumberFactorization factorization) {
        this.cache.put(factorization.getNumber(), factorization);
    }

    /**
     * Gets the current cache hit count.
     * Thread-safe because AtomicLong.get() is atomic.
     * 
     * @return The number of cache hits
     */
    public long getHits() {
        return this.hits.get();
    }

    /**
     * Gets the current cache miss count.
     * Thread-safe because AtomicLong.get() is atomic.
     * 
     * @return The number of cache misses
     */
    public long getMisses() {
        return this.misses.get();
    }

    /**
     * Gets the current size of the cache.
     * Thread-safe because ConcurrentHashMap.size() is atomic.
     * 
     * @return The number of entries in the cache
     */
    public int getCacheSize() {
        return this.cache.size();
    }

    /**
     * Clears the cache.
     * Thread-safe because ConcurrentHashMap.clear() is atomic.
     */
    public void clearCache() {
        this.cache.clear();
    }

    /**
     * Factorizes a number using Sieve of Eratosthenes and caches the result.
     * Thread-safe because:
     * 1. All state modifications use thread-safe operations
     * 2. Local variables are thread-local
     * 3. BigInteger operations are thread-safe
     * 4. ArrayList operations are contained within the method
     * 
     * Optimization: Checks cache for intermediate factors during factorization
     * to avoid redundant calculations for composite numbers.
     * 
     * @param number The number to factorize
     * @return The NumberFactorization containing the number and its factors
     */
    public NumberFactorization factorize(BigInteger number) {
        // Check cache first using thread-safe operations
        NumberFactorization cached = this.getCachedFactorization(number);
        if (cached != null) {
            return cached;
        }

        // Thread-local list for collecting factors
        List<BigInteger> factors = new ArrayList<>();
        BigInteger n = number;
        BigInteger divisor = BigInteger.TWO;

        // Handle even numbers
        while (n.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
            factors.add(BigInteger.TWO);
            n = n.divide(BigInteger.TWO);
            
            // Check if remaining number is in cache
            if (!n.equals(BigInteger.ONE)) {
                NumberFactorization cachedFactor = this.getCachedFactorization(n);
                if (cachedFactor != null) {
                    factors.addAll(cachedFactor.getFactors());
                    n = BigInteger.ONE;
                    break;
                }
            }
        }

        // Check odd numbers up to square root of n
        divisor = BigInteger.valueOf(3);
        while (divisor.multiply(divisor).compareTo(n) <= 0) {
            while (n.mod(divisor).equals(BigInteger.ZERO)) {
                factors.add(divisor);
                n = n.divide(divisor);
                
                // Check if remaining number is in cache
                if (!n.equals(BigInteger.ONE)) {
                    NumberFactorization cachedFactor = this.getCachedFactorization(n);
                    if (cachedFactor != null) {
                        factors.addAll(cachedFactor.getFactors());
                        n = BigInteger.ONE;
                        break;
                    }
                }
            }
            divisor = divisor.add(BigInteger.TWO);
        }

        // If n is still greater than 1, it's a prime number
        if (n.compareTo(BigInteger.ONE) > 0) {
            factors.add(n);
        }

        // Create and cache the factorization using thread-safe operations
        NumberFactorization factorization = new NumberFactorization(number, factors);
        this.updateCache(factorization);
        return factorization;
    }

    /**
     * Demonstrates the functionality of FactorizerWithCaching.
     * Shows how caching works with multiple factorizations and concurrent access.
     * Note: This is a single-threaded demonstration. In real usage,
     * the class would be accessed by multiple threads concurrently.
     */
    public static void main(String[] args) {
        FactorizerWithCaching factorizer = new FactorizerWithCaching();
        
        // Test numbers to factorize
        BigInteger[] numbers = {
            BigInteger.valueOf(100),
            BigInteger.valueOf(1000),
            BigInteger.valueOf(10000),
            BigInteger.valueOf(100000)
        };

        System.out.println("Starting factorization demonstration...\n");

        // First pass - all should be cache misses
        System.out.println("First pass - all should be cache misses:");
        for (BigInteger number : numbers) {
            NumberFactorization result = factorizer.factorize(number);
            System.out.printf("Number: %d%n", number);
            System.out.printf("Factors: %s%n", result.getFactors());
            System.out.printf("Cache hits: %d, misses: %d%n", 
                factorizer.getHits(), factorizer.getMisses());
            System.out.println();
        }

        // Second pass - all should be cache hits
        System.out.println("Second pass - all should be cache hits:");
        for (BigInteger number : numbers) {
            NumberFactorization result = factorizer.factorize(number);
            System.out.printf("Number: %d%n", number);
            System.out.printf("Factors: %s%n", result.getFactors());
            System.out.printf("Cache hits: %d, misses: %d%n", 
                factorizer.getHits(), factorizer.getMisses());
            System.out.println();
        }

        // Demonstrate cache size
        System.out.printf("Final cache size: %d%n", factorizer.getCacheSize());
        
        // Clear cache and verify
        factorizer.clearCache();
        System.out.printf("Cache size after clearing: %d%n", factorizer.getCacheSize());
    }
} 