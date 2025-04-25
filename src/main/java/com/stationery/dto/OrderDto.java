package com.stationery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private UUID id;
    private String orderNo;

    @NotNull(message = "Item ID is required")
    private Integer itemId;

    private String itemName; // untuk kebutuhan view saja

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    private Integer qty;

    // Price tidak lagi dianggap wajib saat membuat order karena ambil dari item
    private Double price;

    // tambahan field untuk langsung melihat total
    private Double totalPrice; // qty * price
}