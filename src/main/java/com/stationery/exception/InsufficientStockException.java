package com.stationery.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(String itemName, Integer requestedQty, Integer availableQty) {
        super(String.format("Insufficient stock for item '%s'. Requested: %d, Available: %d",
                itemName, requestedQty, availableQty));
    }
}