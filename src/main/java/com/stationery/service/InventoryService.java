package com.stationery.service;

import com.stationery.dto.InventoryDto;
import com.stationery.entity.Inventory;
import com.stationery.entity.Item;
import com.stationery.entity.Order;
import com.stationery.enums.InventoryType;
import com.stationery.exception.ResourceNotFoundException;
import com.stationery.repository.InventoryRepository;
import com.stationery.repository.ItemRepository;
import com.stationery.repository.OrderRepository;
import com.stationery.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final ValidationUtil validationUtil;

    /**
     * Get inventory by ID
     */
    @Transactional(readOnly = true)
    public InventoryDto getInventory(Integer id) {
        log.debug("Getting inventory by ID: {}", id);
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", id));

        return mapToInventoryDto(inventory);
    }

    /**
     * Get all inventories with pagination
     */
    @Transactional(readOnly = true)
    public Page<InventoryDto> getAllInventories(Pageable pageable) {
        log.debug("Getting all inventories, page: {}", pageable.getPageNumber());
        Page<Inventory> inventoryPage = inventoryRepository.findAll(pageable);

        List<InventoryDto> inventoryDtos = inventoryPage.getContent().stream()
                .map(this::mapToInventoryDto)
                .collect(Collectors.toList());

        return new PageImpl<>(inventoryDtos, pageable, inventoryPage.getTotalElements());
    }

    /**
     * Get inventory by item ID with pagination
     */
    @Transactional(readOnly = true)
    public Page<InventoryDto> getInventoriesByItemId(Integer itemId, Pageable pageable) {
        log.debug("Getting inventories for item ID: {}, page: {}", itemId, pageable.getPageNumber());

        // Check if item exists
        if (!itemRepository.existsById(itemId)) {
            throw new ResourceNotFoundException("Item", "id", itemId);
        }

        Page<Inventory> inventoryPage = inventoryRepository.findByItemId(itemId, pageable);

        List<InventoryDto> inventoryDtos = inventoryPage.getContent().stream()
                .map(this::mapToInventoryDto)
                .collect(Collectors.toList());

        return new PageImpl<>(inventoryDtos, pageable, inventoryPage.getTotalElements());
    }

    /**
     * Calculate remaining stock for an item
     */
    @Transactional(readOnly = true)
    public Integer calculateRemainingStock(Integer itemId) {
        log.debug("Calculating remaining stock for item ID: {}", itemId);

        // Check if item exists
        if (!itemRepository.existsById(itemId)) {
            throw new ResourceNotFoundException("Item", "id", itemId);
        }

        return inventoryRepository.calculateRemainingStock(itemId);
    }

    /**
     * Create a new inventory entry
     */
    @Transactional
    public InventoryDto createInventory(InventoryDto inventoryDto) {
        log.debug("Creating inventory: {}", inventoryDto);

        Item item = itemRepository.findById(inventoryDto.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", inventoryDto.getItemId()));

        Order order = null;
        if (inventoryDto.getOrderId() != null) {
            order = orderRepository.findById(inventoryDto.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order", "id", inventoryDto.getOrderId()));
        }

        // Untuk validasi stock availability pada withdrawal manual
        if (inventoryDto.getType() == InventoryType.W && order == null) {
            var currentStock = inventoryRepository.calculateRemainingStock(inventoryDto.getItemId());

            // Menggunakan Java 21 String templates untuk pesan error yang lebih bersih
            if (currentStock < inventoryDto.getQty()) {
                var errorMessage = STR."Insufficient stock for item '\{item.getName()}'. Available: \{currentStock}, Requested: \{inventoryDto.getQty()}";
                validationUtil.validateCondition(false, errorMessage);
            }
        }

        Inventory inventory = Inventory.builder()
                .item(item)
                .qty(inventoryDto.getQty())
                .type(inventoryDto.getType())
                .order(order)
                .build();

        Inventory savedInventory = inventoryRepository.save(inventory);
        log.info("Created inventory with ID: {}", savedInventory.getId());

        return mapToInventoryDto(savedInventory);
    }

    /**
     * Update an existing inventory entry
     */
    @Transactional
    public InventoryDto updateInventory(Integer id, InventoryDto inventoryDto) {
        log.debug("Updating inventory with ID: {}", id);

        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", id));

        // Tidak bisa update inventory yang ada referensi ke order
        validationUtil.validateCondition(inventory.getOrder() == null,
                "Cannot update inventory that is linked to an order");

        Item item = itemRepository.findById(inventoryDto.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", inventoryDto.getItemId()));

        // Cek ketersediaan stok jika withdrawal
        if (inventoryDto.getType() == InventoryType.W &&
                (!inventory.getItem().getId().equals(inventoryDto.getItemId()) ||
                        !inventory.getQty().equals(inventoryDto.getQty()))) {

            // Hitung stok saat ini tanpa id inventory terkait
            List<Inventory> allInventories = inventoryRepository.findByItemId(inventoryDto.getItemId());
            int currentStock = allInventories.stream()
                    .filter(inv -> !inv.getId().equals(id))
                    .mapToInt(inv -> inv.getType() == InventoryType.T ? inv.getQty() : -inv.getQty())
                    .sum();

            validationUtil.validateCondition(currentStock >= inventoryDto.getQty(),
                    String.format("Insufficient stock for item '%s'. Available: %d, Requested for withdrawal: %d",
                            item.getName(), currentStock, inventoryDto.getQty()));
        }

        inventory.setItem(item);
        inventory.setQty(inventoryDto.getQty());
        inventory.setType(inventoryDto.getType());

        Inventory updatedInventory = inventoryRepository.save(inventory);
        log.info("Updated inventory with ID: {}", updatedInventory.getId());

        return mapToInventoryDto(updatedInventory);
    }

    /**
     * Delete an inventory entry
     */
    @Transactional
    public void deleteInventory(Integer id) {
        log.debug("Deleting inventory with ID: {}", id);

        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", id));

        // Tidak boleh hapus inventory yang memiliki order
        validationUtil.validateCondition(inventory.getOrder() == null,
                "Cannot delete inventory that is linked to an order. Delete the order first.");

        // Jika  withdrawal, periksa jika menghapus dapat membuat stok minus
        if (inventory.getType() == InventoryType.W) {
            List<Inventory> allInventories = inventoryRepository.findByItemId(inventory.getItem().getId());
            int stockWithoutThisEntry = allInventories.stream()
                    .filter(inv -> !inv.getId().equals(id))
                    .mapToInt(inv -> inv.getType() == InventoryType.T ? inv.getQty() : -inv.getQty())
                    .sum();

            boolean wouldCauseNegativeStock = allInventories.stream()
                    .filter(inv -> !inv.getId().equals(id) && inv.getType() == InventoryType.W)
                    .anyMatch(inv -> {
                        int tempStock = stockWithoutThisEntry;
                        tempStock -= inv.getQty();
                        return tempStock < 0;
                    });

            validationUtil.validateCondition(!wouldCauseNegativeStock,
                    "Cannot delete this withdrawal as it would cause negative stock for other withdrawals.");
        }

        inventoryRepository.delete(inventory);
        log.info("Deleted inventory with ID: {}", id);
    }

    /**
     * Create withdrawal inventory for an order
     */
    @Transactional
    public void createWithdrawalForOrder(Order order) {
        log.debug("Creating withdrawal inventory for order: {}", order.getOrderNo());

        Inventory withdrawal = Inventory.builder()
                .item(order.getItem())
                .qty(order.getQty())
                .type(InventoryType.W)
                .order(order)
                .build();

        inventoryRepository.save(withdrawal);
        log.info("Created withdrawal inventory for order ID: {}", order.getId());
    }

    /**
     * Delete inventory entries associated with an order
     */
    @Transactional
    public void deleteInventoriesForOrder(UUID orderId) {
        log.debug("Deleting inventories for order ID: {}", orderId);
        inventoryRepository.deleteByOrderId(orderId);
        log.info("Deleted inventories for order ID: {}", orderId);
    }

    /**
     * Map Inventory entity to InventoryDto
     */
    private InventoryDto mapToInventoryDto(Inventory inventory) {
        InventoryDto dto = InventoryDto.builder()
                .id(inventory.getId())
                .itemId(inventory.getItem().getId())
                .itemName(inventory.getItem().getName())
                .qty(inventory.getQty())
                .type(inventory.getType())
                .build();

        if (inventory.getOrder() != null) {
            dto.setOrderId(inventory.getOrder().getId());
        }

        return dto;
    }
}