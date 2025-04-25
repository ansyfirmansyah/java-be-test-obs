package com.stationery.util;

import com.stationery.exception.BusinessLogicException;
import org.springframework.stereotype.Component;

/**
 * Utility class for validation logic that can be reused across the application.
 */
@Component
public class ValidationUtil {

    /**
     * Validates if a condition is true, throws BusinessLogicException if not.
     * @param condition the condition to check
     * @param message the error message to include if the condition is false
     */
    public void validateCondition(boolean condition, String message) {
        if (!condition) {
            throw new BusinessLogicException(message);
        }
    }
}