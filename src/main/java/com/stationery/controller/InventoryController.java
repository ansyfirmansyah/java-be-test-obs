package com.stationery.controller;

import com.stationery.dto.InventoryDto;
import com.stationery.dto.response.ApiResponse;
import com.stationery.dto.response.PageResponse;
import com.stationery.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryDto>> getInventory(@PathVariable Integer id) {
        log.info("Request to get inventory with ID: {}", id);
        InventoryDto inventory = inventoryService.getInventory(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Inventory retrieved successfully", inventory));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<InventoryDto>>> getAllInventories(Pageable pageable) {
        log.info("Request to get all inventories, page: {}", pageable.getPageNumber());
        Page<InventoryDto> inventories = inventoryService.getAllInventories(pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Inventories retrieved successfully",
                PageResponse.fromPage(inventories)));
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<ApiResponse<PageResponse<InventoryDto>>> getInventoriesByItemId(
            @PathVariable Integer itemId, Pageable pageable) {
        log.info("Request to get inventories for item ID: {}, page: {}", itemId, pageable.getPageNumber());
        Page<InventoryDto> inventories = inventoryService.getInventoriesByItemId(itemId, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Inventories retrieved successfully",
                PageResponse.fromPage(inventories)));
    }

    @GetMapping("/stock/{itemId}")
    public ResponseEntity<ApiResponse<Integer>> getStockByItemId(@PathVariable Integer itemId) {
        log.info("Request to get stock for item ID: {}", itemId);
        Integer stock = inventoryService.calculateRemainingStock(itemId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Stock retrieved successfully", stock));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InventoryDto>> createInventory(@Valid @RequestBody InventoryDto inventoryDto) {
        log.info("Request to create inventory: {}", inventoryDto);
        InventoryDto createdInventory = inventoryService.createInventory(inventoryDto);
        return new ResponseEntity<>(new ApiResponse<>(true, "Inventory created successfully", createdInventory),
                HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryDto>> updateInventory(
            @PathVariable Integer id, @Valid @RequestBody InventoryDto inventoryDto) {
        log.info("Request to update inventory with ID: {}", id);
        InventoryDto updatedInventory = inventoryService.updateInventory(id, inventoryDto);
        return ResponseEntity.ok(new ApiResponse<>(true, "Inventory updated successfully", updatedInventory));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInventory(@PathVariable Integer id) {
        log.info("Request to delete inventory with ID: {}", id);
        inventoryService.deleteInventory(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Inventory deleted successfully", null));
    }
}