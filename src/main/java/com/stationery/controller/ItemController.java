package com.stationery.controller;

import com.stationery.dto.ItemDto;
import com.stationery.dto.response.ApiResponse;
import com.stationery.dto.response.ItemWithStockDto;
import com.stationery.dto.response.PageResponse;
import com.stationery.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemWithStockDto>> getItem(@PathVariable Integer id) {
        log.info("Request to get item with ID: {}", id);
        ItemWithStockDto item = itemService.getItemWithStock(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Item retrieved successfully", item));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ItemWithStockDto>>> getAllItems(Pageable pageable) {
        log.info("Request to get all items with stock, page: {}", pageable.getPageNumber());
        Page<ItemWithStockDto> items = itemService.getAllItemsWithStock(pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Items retrieved successfully",
                PageResponse.fromPage(items)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ItemDto>> createItem(@Valid @RequestBody ItemDto itemDto) {
        log.info("Request to create new item: {}", itemDto.getName());
        ItemDto createdItem = itemService.createItem(itemDto);
        return new ResponseEntity<>(new ApiResponse<>(true, "Item created successfully", createdItem),
                HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemDto>> updateItem(
            @PathVariable Integer id, @Valid @RequestBody ItemDto itemDto) {
        log.info("Request to update item with ID: {}", id);
        ItemDto updatedItem = itemService.updateItem(id, itemDto);
        return ResponseEntity.ok(new ApiResponse<>(true, "Item updated successfully", updatedItem));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Integer id) {
        log.info("Request to delete item with ID: {}", id);
        itemService.deleteItem(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Item deleted successfully", null));
    }
}