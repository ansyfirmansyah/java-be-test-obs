package com.stationery.service;

import com.stationery.dto.OrderDto;
import com.stationery.entity.Item;
import com.stationery.entity.Order;
import com.stationery.exception.InsufficientStockException;
import com.stationery.exception.ResourceNotFoundException;
import com.stationery.repository.InventoryRepository;
import com.stationery.repository.ItemRepository;
import com.stationery.repository.OrderRepository;
import com.stationery.util.OrderNumberGenerator;
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
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryService inventoryService;
    private final OrderNumberGenerator orderNumberGenerator;

    /**
     * Get order by ID
     */
    @Transactional(readOnly = true)
    public OrderDto getOrder(UUID id) {
        log.debug("Getting order by ID: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        return mapToOrderDto(order);
    }

    /**
     * Get all orders with pagination
     */
    @Transactional(readOnly = true)
    public Page<OrderDto> getAllOrders(Pageable pageable) {
        log.debug("Getting all orders, page: {}", pageable.getPageNumber());
        Page<Order> orderPage = orderRepository.findAll(pageable);

        List<OrderDto> orderDtos = orderPage.getContent().stream()
                .map(this::mapToOrderDto)
                .collect(Collectors.toList());

        return new PageImpl<>(orderDtos, pageable, orderPage.getTotalElements());
    }

    /**
     * Get orders by item ID with pagination
     */
    @Transactional(readOnly = true)
    public Page<OrderDto> getOrdersByItemId(Integer itemId, Pageable pageable) {
        log.debug("Getting orders for item ID: {}, page: {}", itemId, pageable.getPageNumber());

        // Check if item exists
        if (!itemRepository.existsById(itemId)) {
            throw new ResourceNotFoundException("Item", "id", itemId);
        }

        Page<Order> orderPage = orderRepository.findByItemId(itemId, pageable);

        List<OrderDto> orderDtos = orderPage.getContent().stream()
                .map(this::mapToOrderDto)
                .collect(Collectors.toList());

        return new PageImpl<>(orderDtos, pageable, orderPage.getTotalElements());
    }

    /**
     * Create a new order
     */
    @Transactional
    public OrderDto createOrder(OrderDto orderDto) {
        log.debug("Creating order: {}", orderDto);

        Item item = itemRepository.findById(orderDto.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", orderDto.getItemId()));

        // Generate order number
        String orderNo = orderNumberGenerator.generateOrderNumber();

        // Check stock availability
        Integer currentStock = inventoryRepository.calculateRemainingStock(orderDto.getItemId());
        if (currentStock < orderDto.getQty()) {
            throw new InsufficientStockException(
                    item.getName(), orderDto.getQty(), currentStock);
        }

        // Create order dengan menggunakan harga dari item, bukan dari input
        Order order = Order.builder()
                .orderNo(orderNo)
                .item(item)
                .qty(orderDto.getQty())
                .price(item.getPrice()) // Selalu gunakan harga dari database
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("Created order with ID: {}", savedOrder.getId());

        // Create withdrawal inventory
        inventoryService.createWithdrawalForOrder(savedOrder);

        return mapToOrderDto(savedOrder);
    }

    /**
     * Update an existing order
     */
    @Transactional
    public OrderDto updateOrder(UUID id, OrderDto orderDto) {
        log.debug("Updating order with ID: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        Item item = itemRepository.findById(orderDto.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", orderDto.getItemId()));

        boolean itemChanged = !order.getItem().getId().equals(item.getId());
        boolean qtyChanged = !order.getQty().equals(orderDto.getQty());

        if (itemChanged || qtyChanged) {
            inventoryService.deleteInventoriesForOrder(id);

            Integer currentStock = inventoryRepository.calculateRemainingStock(item.getId());
            if (currentStock < orderDto.getQty()) {
                throw new InsufficientStockException(
                        item.getName(), orderDto.getQty(), currentStock);
            }

            order.setItem(item);
            order.setQty(orderDto.getQty());
            order.setPrice(orderDto.getPrice());

            Order updatedOrder = orderRepository.save(order);

            inventoryService.createWithdrawalForOrder(updatedOrder);

            log.info("Updated order with ID: {}", updatedOrder.getId());
            return mapToOrderDto(updatedOrder);
        } else {
            // Only price changed, no need to update inventory
            order.setPrice(orderDto.getPrice());
            Order updatedOrder = orderRepository.save(order);
            log.info("Updated order price with ID: {}", updatedOrder.getId());
            return mapToOrderDto(updatedOrder);
        }
    }

    /**
     * Delete an order
     */
    @Transactional
    public void deleteOrder(UUID id) {
        log.debug("Deleting order with ID: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        inventoryService.deleteInventoriesForOrder(id);

        orderRepository.delete(order);
        log.info("Deleted order with ID: {}", id);
    }

    /**
     * Map Order entity to OrderDto
     */
    private OrderDto mapToOrderDto(Order order) {
        return OrderDto.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .itemId(order.getItem().getId())
                .itemName(order.getItem().getName())
                .qty(order.getQty())
                .price(order.getPrice())
                .totalPrice(order.getQty() * order.getPrice())
                .build();
    }
}