package com.stationery.dto.response;

// Using Java Record instead of class with Lombok (Java 16+)
public record ItemWithStockDto(
        Integer id,
        String name,
        Double price,
        Integer stockQuantity
) {
    // Static factory method
    public static ItemWithStockDto of(Integer id, String name, Double price, Integer stockQuantity) {
        return new ItemWithStockDto(id, name, price, stockQuantity);
    }
}