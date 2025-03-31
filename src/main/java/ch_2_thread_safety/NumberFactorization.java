package ch_2_thread_safety;

import java.math.BigInteger;
import java.util.List;

/**
 * Immutable class representing a number and its factors.
 * This class is thread-safe because it's immutable.
 */
public class NumberFactorization {
    private final BigInteger number;
    private final List<BigInteger> factors;

    public NumberFactorization(BigInteger number, List<BigInteger> factors) {
        this.number = number;
        this.factors = factors;
    }

    public BigInteger getNumber() {
        return number;
    }

    public List<BigInteger> getFactors() {
        return factors;
    }

    @Override
    public String toString() {
        return "NumberFactorization{" +
                "number=" + number +
                ", factors=" + factors +
                '}';
    }
} 