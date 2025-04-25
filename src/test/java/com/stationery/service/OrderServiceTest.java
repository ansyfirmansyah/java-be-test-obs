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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private OrderNumberGenerator orderNumberGenerator;

    @InjectMocks
    private OrderService orderService;

    private Item item1;
    private Order order1;
    private OrderDto orderDto1;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        // Setup test data
        item1 = Item.builder()
                .id(1)
                .name("Pensil 2B")
                .price(2500.0)
                .build();

        orderId = UUID.randomUUID();

        order1 = Order.builder()
                .id(orderId)
                .orderNo("O001")
                .item(item1)
                .qty(5)
                .price(2500.0)
                .build();

        orderDto1 = OrderDto.builder()
                .id(orderId)
                .orderNo("O001")
                .itemId(1)
                .itemName("Pensil 2B")
                .qty(5)
                .price(2500.0)
                .totalPrice(12500.0) // 5 * 2500
                .build();
    }

    @Test
    void getOrder_Success() {
        // Given
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order1));

        // When
        OrderDto result = orderService.getOrder(orderId);

        // Then
        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals("O001", result.getOrderNo());
        assertEquals(1, result.getItemId());
        assertEquals("Pensil 2B", result.getItemName());
        assertEquals(5, result.getQty());
        assertEquals(2500.0, result.getPrice());
        assertEquals(12500.0, result.getTotalPrice());
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void getOrder_NotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(orderRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrder(nonExistentId));
        verify(orderRepository, times(1)).findById(nonExistentId);
    }

    @Test
    void getAllOrders_Success() {
        // Given
        List<Order> orders = Arrays.asList(order1);
        Page<Order> page = new PageImpl<>(orders);

        Pageable pageable = PageRequest.of(0, 10);
        when(orderRepository.findAll(pageable)).thenReturn(page);

        // When
        Page<OrderDto> result = orderService.getAllOrders(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("O001", result.getContent().get(0).getOrderNo());
        assertEquals(1, result.getContent().get(0).getItemId());
        verify(orderRepository, times(1)).findAll(pageable);
    }

    @Test
    void getOrdersByItemId_Success() {
        // Given
        List<Order> orders = Arrays.asList(order1);
        Page<Order> page = new PageImpl<>(orders);

        Pageable pageable = PageRequest.of(0, 10);
        when(itemRepository.existsById(1)).thenReturn(true);
        when(orderRepository.findByItemId(1, pageable)).thenReturn(page);

        // When
        Page<OrderDto> result = orderService.getOrdersByItemId(1, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getContent().get(0).getItemId());
        assertEquals("Pensil 2B", result.getContent().get(0).getItemName());
        verify(itemRepository, times(1)).existsById(1);
        verify(orderRepository, times(1)).findByItemId(1, pageable);
    }

    @Test
    void getOrdersByItemId_ItemNotFound() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(itemRepository.existsById(99)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                orderService.getOrdersByItemId(99, pageable));
        verify(itemRepository, times(1)).existsById(99);
        verify(orderRepository, never()).findByItemId(anyInt(), any(Pageable.class));
    }

    @Test
    void createOrder_WithSufficientStock_Success() {
        // Given
        OrderDto newOrderDto = OrderDto.builder()
                .itemId(1)
                .qty(10)
                .build();

        Order savedOrder = Order.builder()
                .id(orderId)
                .orderNo("O002")
                .item(item1)
                .qty(10)
                .price(2500.0)
                .build();

        when(itemRepository.findById(1)).thenReturn(Optional.of(item1));
        when(orderNumberGenerator.generateOrderNumber()).thenReturn("O002");
        when(inventoryRepository.calculateRemainingStock(1)).thenReturn(100);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        doNothing().when(inventoryService).createWithdrawalForOrder(any(Order.class));

        // When
        OrderDto result = orderService.createOrder(newOrderDto);

        // Then
        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals("O002", result.getOrderNo());
        assertEquals(1, result.getItemId());
        assertEquals(10, result.getQty());
        assertEquals(2500.0, result.getPrice());
        assertEquals(25000.0, result.getTotalPrice());
        verify(itemRepository, times(1)).findById(1);
        verify(orderNumberGenerator, times(1)).generateOrderNumber();
        verify(inventoryRepository, times(1)).calculateRemainingStock(1);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(inventoryService, times(1)).createWithdrawalForOrder(any(Order.class));
    }

    @Test
    void createOrder_WithInsufficientStock() {
        // Given
        OrderDto newOrderDto = OrderDto.builder()
                .itemId(1)
                .qty(150)
                .build();

        when(itemRepository.findById(1)).thenReturn(Optional.of(item1));
        when(orderNumberGenerator.generateOrderNumber()).thenReturn("O002");
        when(inventoryRepository.calculateRemainingStock(1)).thenReturn(100);

        // When & Then
        assertThrows(InsufficientStockException.class, () -> orderService.createOrder(newOrderDto));
        verify(itemRepository, times(1)).findById(1);
        verify(orderNumberGenerator, times(1)).generateOrderNumber();
        verify(inventoryRepository, times(1)).calculateRemainingStock(1);
        verify(orderRepository, never()).save(any(Order.class));
        verify(inventoryService, never()).createWithdrawalForOrder(any(Order.class));
    }

    @Test
    void updateOrder_WithItemAndQtyChange_Success() {
        // Given
        OrderDto updateOrderDto = OrderDto.builder()
                .id(orderId)
                .itemId(1)
                .qty(8)
                .price(2500.0)
                .build();

        Order updatedOrder = Order.builder()
                .id(orderId)
                .orderNo("O001")
                .item(item1)
                .qty(8)
                .price(2500.0)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order1));
        when(itemRepository.findById(1)).thenReturn(Optional.of(item1));
        doNothing().when(inventoryService).deleteInventoriesForOrder(orderId);
        when(inventoryRepository.calculateRemainingStock(1)).thenReturn(100);
        when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);
        doNothing().when(inventoryService).createWithdrawalForOrder(any(Order.class));

        // When
        OrderDto result = orderService.updateOrder(orderId, updateOrderDto);

        // Then
        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals("O001", result.getOrderNo());
        assertEquals(1, result.getItemId());
        assertEquals(8, result.getQty());
        assertEquals(2500.0, result.getPrice());
        assertEquals(20000.0, result.getTotalPrice());
        verify(orderRepository, times(1)).findById(orderId);
        verify(itemRepository, times(1)).findById(1);
        verify(inventoryService, times(1)).deleteInventoriesForOrder(orderId);
        verify(inventoryRepository, times(1)).calculateRemainingStock(1);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(inventoryService, times(1)).createWithdrawalForOrder(any(Order.class));
    }

    @Test
    void updateOrder_OnlyPriceChange_Success() {
        // Given
        OrderDto updateOrderDto = OrderDto.builder()
                .id(orderId)
                .itemId(1)
                .qty(5)
                .price(3000.0) // Changed price
                .build();

        Order updatedOrder = Order.builder()
                .id(orderId)
                .orderNo("O001")
                .item(item1)
                .qty(5)
                .price(3000.0)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order1));
        when(itemRepository.findById(1)).thenReturn(Optional.of(item1));
        when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);

        // When
        OrderDto result = orderService.updateOrder(orderId, updateOrderDto);

        // Then
        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals("O001", result.getOrderNo());
        assertEquals(1, result.getItemId());
        assertEquals(5, result.getQty());
        assertEquals(3000.0, result.getPrice());
        assertEquals(15000.0, result.getTotalPrice());
        verify(orderRepository, times(1)).findById(orderId);
        verify(itemRepository, times(1)).findById(1);
        verify(inventoryService, never()).deleteInventoriesForOrder(any(UUID.class));
        verify(inventoryRepository, never()).calculateRemainingStock(anyInt());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(inventoryService, never()).createWithdrawalForOrder(any(Order.class));
    }

    @Test
    void updateOrder_WithInsufficientStock() {
        // Given
        OrderDto updateOrderDto = OrderDto.builder()
                .id(orderId)
                .itemId(1)
                .qty(150) // Large quantity
                .price(2500.0)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order1));
        when(itemRepository.findById(1)).thenReturn(Optional.of(item1));
        doNothing().when(inventoryService).deleteInventoriesForOrder(orderId);
        when(inventoryRepository.calculateRemainingStock(1)).thenReturn(100);

        // When & Then
        assertThrows(InsufficientStockException.class, () ->
                orderService.updateOrder(orderId, updateOrderDto));
        verify(orderRepository, times(1)).findById(orderId);
        verify(itemRepository, times(1)).findById(1);
        verify(inventoryService, times(1)).deleteInventoriesForOrder(orderId);
        verify(inventoryRepository, times(1)).calculateRemainingStock(1);
        verify(orderRepository, never()).save(any(Order.class));
        verify(inventoryService, never()).createWithdrawalForOrder(any(Order.class));
    }

    @Test
    void deleteOrder_Success() {
        // Given
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order1));
        doNothing().when(inventoryService).deleteInventoriesForOrder(orderId);
        doNothing().when(orderRepository).delete(order1);

        // When
        orderService.deleteOrder(orderId);

        // Then
        verify(orderRepository, times(1)).findById(orderId);
        verify(inventoryService, times(1)).deleteInventoriesForOrder(orderId);
        verify(orderRepository, times(1)).delete(order1);
    }

    @Test
    void deleteOrder_NotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(orderRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> orderService.deleteOrder(nonExistentId));
        verify(orderRepository, times(1)).findById(nonExistentId);
        verify(inventoryService, never()).deleteInventoriesForOrder(any(UUID.class));
        verify(orderRepository, never()).delete(any(Order.class));
    }
}