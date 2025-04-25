package com.stationery.util;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class to generate sequential order numbers in the format Oxxx,
 * where xxx is a sequential number.
 */
@Component
public class OrderNumberGenerator {

    private final AtomicInteger counter = new AtomicInteger(1);

    /**
     * Generates a new order number in the format Oxxx
     * @return the generated order number
     */
    public String generateOrderNumber() {
        // Using text block for better readability (Java 15+)
        var sequence = counter.getAndIncrement();
        return STR."O\{String.format("%03d", sequence)}";
    }
}