package com.stationery.service;

import com.stationery.dto.ItemDto;
import com.stationery.dto.response.ItemWithStockDto;
import com.stationery.entity.Item;
import com.stationery.exception.ResourceNotFoundException;
import com.stationery.repository.InventoryRepository;
import com.stationery.repository.ItemRepository;
import com.stationery.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;
    private final InventoryRepository inventoryRepository;
    private final ValidationUtil validationUtil;

    /**
     * Get item by ID with stock information
     */
    @Transactional(readOnly = true)
    public ItemWithStockDto getItemWithStock(Integer id) {
        log.debug("Getting item with stock by ID: {}", id);

        // Ambil item dan hitung stock secara terpisah untuk menghindari masalah casting
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));

        // Hitung stock secara langsung
        Integer stockCount = inventoryRepository.calculateRemainingStock(id);
        log.debug("Stock count for item {}: {}", id, stockCount);

        return mapToItemWithStockDto(item, stockCount);
    }

    /**
     * Get item by ID without stock information
     */
    @Transactional(readOnly = true)
    public ItemDto getItem(Integer id) {
        log.debug("Getting item by ID: {}", id);
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));

        return mapToItemDto(item);
    }

    /**
     * Get all items with stock information
     */
    @Transactional(readOnly = true)
    public Page<ItemWithStockDto> getAllItemsWithStock(Pageable pageable) {
        log.debug("Getting all items with stock, page: {}", pageable.getPageNumber());

        long totalItems = itemRepository.count();

        Page<Item> itemPage = itemRepository.findAll(pageable);

        List<ItemWithStockDto> itemDtos = itemPage.getContent().stream()
                .map(item -> {
                    Integer stockCount = inventoryRepository.calculateRemainingStock(item.getId());
                    return mapToItemWithStockDto(item, stockCount);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(itemDtos, pageable, totalItems);
    }

    /**
     * Create a new item
     */
    @Transactional
    public ItemDto createItem(ItemDto itemDto) {
        log.debug("Creating new item: {}", itemDto.getName());
        Item item = Item.builder()
                .name(itemDto.getName())
                .price(itemDto.getPrice())
                .build();

        Item savedItem = itemRepository.save(item);
        log.info("Created new item with ID: {}", savedItem.getId());

        return mapToItemDto(savedItem);
    }

    /**
     * Update an existing item
     */
    @Transactional
    public ItemDto updateItem(Integer id, ItemDto itemDto) {
        log.debug("Updating item with ID: {}", id);
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));

        item.setName(itemDto.getName());
        item.setPrice(itemDto.getPrice());

        Item updatedItem = itemRepository.save(item);
        log.info("Updated item with ID: {}", updatedItem.getId());

        return mapToItemDto(updatedItem);
    }

    /**
     * Delete an item if it has no inventory entries
     */
    @Transactional
    public void deleteItem(Integer id) {
        log.debug("Deleting item with ID: {}", id);
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));

        // Check if item has inventory entries - updated for Spring Boot 3
        boolean hasInventory = itemRepository.hasInventories(id);
        validationUtil.validateCondition(!hasInventory,
                "Cannot delete item. Item has inventory entries. Delete inventory first.");

        itemRepository.delete(item);
        log.info("Deleted item with ID: {}", id);
    }

    /**
     * Map Item entity to ItemDto
     */
    private ItemDto mapToItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .price(item.getPrice())
                .build();
    }

    /**
     * Map Item entity to ItemWithStockDto
     */
    private ItemWithStockDto mapToItemWithStockDto(Item item, Integer stockQuantity) {
        // Menggunakan record constructor secara langsung (Java 16+)
        return new ItemWithStockDto(
                item.getId(),
                item.getName(),
                item.getPrice(),
                stockQuantity
        );
    }
}