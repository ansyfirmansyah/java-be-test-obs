package com.stationery.dto;

import com.stationery.enums.InventoryType;
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
public class InventoryDto {
    private Integer id;

    @NotNull(message = "Item ID is required")
    private Integer itemId;

    private String itemName; // untuk kebutuhan view saja

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    private Integer qty;

    @NotNull(message = "Type is required")
    private InventoryType type;

    private UUID orderId; // Opsional, sebagai referensi dengan table order jika perlu rollback
}