package com.stationery.service;

import com.stationery.dto.InventoryDto;
import com.stationery.entity.Inventory;
import com.stationery.entity.Item;
import com.stationery.entity.Order;
import com.stationery.enums.InventoryType;
import com.stationery.exception.BusinessLogicException;
import com.stationery.exception.ResourceNotFoundException;
import com.stationery.repository.InventoryRepository;
import com.stationery.repository.ItemRepository;
import com.stationery.repository.OrderRepository;
import com.stationery.util.ValidationUtil;
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
public class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ValidationUtil validationUtil;

    @InjectMocks
    private InventoryService inventoryService;

    private Item item1;
    private Inventory topUpInventory;
    private Inventory withdrawalInventory;
    private InventoryDto topUpDto;
    private InventoryDto withdrawalDto;
    private Order order1;
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

        topUpInventory = Inventory.builder()
                .id(1)
                .item(item1)
                .qty(100)
                .type(InventoryType.T)
                .order(null)
                .build();

        withdrawalInventory = Inventory.builder()
                .id(2)
                .item(item1)
                .qty(20)
                .type(InventoryType.W)
                .order(order1)
                .build();

        topUpDto = InventoryDto.builder()
                .id(1)
                .itemId(1)
                .itemName("Pensil 2B")
                .qty(100)
                .type(InventoryType.T)
                .orderId(null)
                .build();

        withdrawalDto = InventoryDto.builder()
                .id(2)
                .itemId(1)
                .itemName("Pensil 2B")
                .qty(20)
                .type(InventoryType.W)
                .orderId(orderId)
                .build();
    }

    @Test
    void getInventory_Success() {
        // Given
        when(inventoryRepository.findById(1)).thenReturn(Optional.of(topUpInventory));

        // When
        InventoryDto result = inventoryService.getInventory(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(1, result.getItemId());
        assertEquals("Pensil 2B", result.getItemName());
        assertEquals(100, result.getQty());
        assertEquals(InventoryType.T, result.getType());
        assertNull(result.getOrderId());
        verify(inventoryRepository, times(1)).findById(1);
    }

    @Test
    void getInventory_NotFound() {
        // Given
        when(inventoryRepository.findById(99)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> inventoryService.getInventory(99));
        verify(inventoryRepository, times(1)).findById(99);
    }

    @Test
    void getAllInventories_Success() {
        // Given
        List<Inventory> inventories = Arrays.asList(topUpInventory, withdrawalInventory);
        Page<Inventory> page = new PageImpl<>(inventories);

        Pageable pageable = PageRequest.of(0, 10);
        when(inventoryRepository.findAll(pageable)).thenReturn(page);

        // When
        Page<InventoryDto> result = inventoryService.getAllInventories(pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(InventoryType.T, result.getContent().get(0).getType());
        assertEquals(InventoryType.W, result.getContent().get(1).getType());
        verify(inventoryRepository, times(1)).findAll(pageable);
    }

    @Test
    void getInventoriesByItemId_Success() {
        // Given
        List<Inventory> inventories = Arrays.asList(topUpInventory, withdrawalInventory);
        Page<Inventory> page = new PageImpl<>(inventories);

        Pageable pageable = PageRequest.of(0, 10);
        when(itemRepository.existsById(1)).thenReturn(true);
        when(inventoryRepository.findByItemId(1, pageable)).thenReturn(page);

        // When
        Page<InventoryDto> result = inventoryService.getInventoriesByItemId(1, pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(1, result.getContent().get(0).getItemId());
        assertEquals(1, result.getContent().get(1).getItemId());
        verify(itemRepository, times(1)).existsById(1);
        verify(inventoryRepository, times(1)).findByItemId(1, pageable);
    }

    @Test
    void getInventoriesByItemId_ItemNotFound() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(itemRepository.existsById(99)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                inventoryService.getInventoriesByItemId(99, pageable));
        verify(itemRepository, times(1)).existsById(99);
        verify(inventoryRepository, never()).findByItemId(anyInt(), any(Pageable.class));
    }

    @Test
    void calculateRemainingStock_Success() {
        // Given
        when(itemRepository.existsById(1)).thenReturn(true);
        when(inventoryRepository.calculateRemainingStock(1)).thenReturn(80);

        // When
        Integer result = inventoryService.calculateRemainingStock(1);

        // Then
        assertEquals(80, result);
        verify(itemRepository, times(1)).existsById(1);
        verify(inventoryRepository, times(1)).calculateRemainingStock(1);
    }

    @Test
    void createInventory_TopUp_Success() {
        // Given
        InventoryDto newTopUpDto = InventoryDto.builder()
                .itemId(1)
                .qty(50)
                .type(InventoryType.T)
                .build();

        when(itemRepository.findById(1)).thenReturn(Optional.of(item1));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(topUpInventory);

        // When
        InventoryDto result = inventoryService.createInventory(newTopUpDto);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(1, result.getItemId());
        assertEquals(100, result.getQty());
        assertEquals(InventoryType.T, result.getType());
        verify(itemRepository, times(1)).findById(1);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void createInventory_Withdrawal_WithSufficientStock_Success() {
        // Given
        InventoryDto newWithdrawalDto = InventoryDto.builder()
                .itemId(1)
                .qty(30)
                .type(InventoryType.W)
                .build();

        when(itemRepository.findById(1)).thenReturn(Optional.of(item1));
        when(inventoryRepository.calculateRemainingStock(1)).thenReturn(100);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(withdrawalInventory);

        // When
        InventoryDto result = inventoryService.createInventory(newWithdrawalDto);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getId());
        assertEquals(1, result.getItemId());
        assertEquals(20, result.getQty());
        assertEquals(InventoryType.W, result.getType());
        verify(itemRepository, times(1)).findById(1);
        verify(inventoryRepository, times(1)).calculateRemainingStock(1);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void createInventory_Withdrawal_InsufficientStock() {
        // Given
        InventoryDto newWithdrawalDto = InventoryDto.builder()
                .itemId(1)
                .qty(150)
                .type(InventoryType.W)
                .build();

        when(itemRepository.findById(1)).thenReturn(Optional.of(item1));
        when(inventoryRepository.calculateRemainingStock(1)).thenReturn(100);
        doThrow(new BusinessLogicException("Insufficient stock for item 'Pensil 2B'. Available: 100, Requested: 150"))
                .when(validationUtil).validateCondition(eq(false), anyString());

        // When & Then
        assertThrows(BusinessLogicException.class, () -> inventoryService.createInventory(newWithdrawalDto));
        verify(itemRepository, times(1)).findById(1);
        verify(inventoryRepository, times(1)).calculateRemainingStock(1);
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void deleteInventory_NotLinkedToOrder_Success() {
        // Given
        when(inventoryRepository.findById(1)).thenReturn(Optional.of(topUpInventory));
        doNothing().when(inventoryRepository).delete(topUpInventory);

        // When
        inventoryService.deleteInventory(1);

        // Then
        verify(inventoryRepository, times(1)).findById(1);
        verify(inventoryRepository, times(1)).delete(topUpInventory);
    }

    @Test
    void deleteInventory_LinkedToOrder_ShouldFail() {
        // Given
        when(inventoryRepository.findById(2)).thenReturn(Optional.of(withdrawalInventory));
        doThrow(new BusinessLogicException("Cannot delete inventory that is linked to an order. Delete the order first."))
                .when(validationUtil).validateCondition(eq(false), anyString());

        // When & Then
        assertThrows(BusinessLogicException.class, () -> inventoryService.deleteInventory(2));
        verify(inventoryRepository, times(1)).findById(2);
        verify(inventoryRepository, never()).delete(any(Inventory.class));
    }

    @Test
    void createWithdrawalForOrder_Success() {
        // Given
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(withdrawalInventory);

        // When
        inventoryService.createWithdrawalForOrder(order1);

        // Then
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void deleteInventoriesForOrder_Success() {
        // Given
        doNothing().when(inventoryRepository).deleteByOrderId(orderId);

        // When
        inventoryService.deleteInventoriesForOrder(orderId);

        // Then
        verify(inventoryRepository, times(1)).deleteByOrderId(orderId);
    }
}