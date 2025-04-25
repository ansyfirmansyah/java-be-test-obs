package com.stationery.service;

import com.stationery.dto.ItemDto;
import com.stationery.dto.response.ItemWithStockDto;
import com.stationery.entity.Item;
import com.stationery.exception.BusinessLogicException;
import com.stationery.exception.ResourceNotFoundException;
import com.stationery.repository.InventoryRepository;
import com.stationery.repository.ItemRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ValidationUtil validationUtil;

    @InjectMocks
    private ItemService itemService;

    private Item item1;
    private Item item2;
    private ItemDto itemDto1;

    @BeforeEach
    void setUp() {
        // Setup test data
        item1 = Item.builder()
                .id(1)
                .name("Pensil 2B")
                .price(2500.0)
                .build();

        item2 = Item.builder()
                .id(2)
                .name("Buku Tulis")
                .price(5000.0)
                .build();

        itemDto1 = ItemDto.builder()
                .id(1)
                .name("Pensil 2B")
                .price(2500.0)
                .build();
    }

    @Test
    void getItemWithStock_Success() {
        // Given
        when(itemRepository.findById(1)).thenReturn(Optional.of(item1));
        when(inventoryRepository.calculateRemainingStock(1)).thenReturn(100);

        // When
        ItemWithStockDto result = itemService.getItemWithStock(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.id());
        assertEquals("Pensil 2B", result.name());
        assertEquals(2500.0, result.price());
        assertEquals(100, result.stockQuantity());
        verify(itemRepository, times(1)).findById(1);
        verify(inventoryRepository, times(1)).calculateRemainingStock(1);
    }

    @Test
    void getItemWithStock_NotFound() {
        // Given
        when(itemRepository.findById(99)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> itemService.getItemWithStock(99));
        verify(itemRepository, times(1)).findById(99);
        verify(inventoryRepository, never()).calculateRemainingStock(anyInt());
    }

    @Test
    void getItem_Success() {
        // Given
        when(itemRepository.findById(1)).thenReturn(Optional.of(item1));

        // When
        ItemDto result = itemService.getItem(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Pensil 2B", result.getName());
        assertEquals(2500.0, result.getPrice());
        verify(itemRepository, times(1)).findById(1);
    }

    @Test
    void getAllItemsWithStock_Success() {
        // Given
        List<Item> items = Arrays.asList(item1, item2);
        Page<Item> itemPage = new PageImpl<>(items);

        when(itemRepository.count()).thenReturn(2L);
        when(itemRepository.findAll(any(Pageable.class))).thenReturn(itemPage);
        when(inventoryRepository.calculateRemainingStock(1)).thenReturn(100);
        when(inventoryRepository.calculateRemainingStock(2)).thenReturn(50);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<ItemWithStockDto> result = itemService.getAllItemsWithStock(pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals("Pensil 2B", result.getContent().get(0).name());
        assertEquals(100, result.getContent().get(0).stockQuantity());
        assertEquals("Buku Tulis", result.getContent().get(1).name());
        assertEquals(50, result.getContent().get(1).stockQuantity());
        verify(itemRepository, times(1)).count();
        verify(itemRepository, times(1)).findAll(any(Pageable.class));
        verify(inventoryRepository, times(1)).calculateRemainingStock(1);
        verify(inventoryRepository, times(1)).calculateRemainingStock(2);
    }

    @Test
    void createItem_Success() {
        // Given
        Item newItem = Item.builder()
                .name("Spidol")
                .price(7500.0)
                .build();

        Item savedItem = Item.builder()
                .id(3)
                .name("Spidol")
                .price(7500.0)
                .build();

        ItemDto newItemDto = ItemDto.builder()
                .name("Spidol")
                .price(7500.0)
                .build();

        when(itemRepository.save(any(Item.class))).thenReturn(savedItem);

        // When
        ItemDto result = itemService.createItem(newItemDto);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getId());
        assertEquals("Spidol", result.getName());
        assertEquals(7500.0, result.getPrice());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void updateItem_Success() {
        // Given
        ItemDto updateDto = ItemDto.builder()
                .id(1)
                .name("Pensil 2B Updated")
                .price(3000.0)
                .build();

        Item updatedItem = Item.builder()
                .id(1)
                .name("Pensil 2B Updated")
                .price(3000.0)
                .build();

        when(itemRepository.findById(1)).thenReturn(Optional.of(item1));
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);

        // When
        ItemDto result = itemService.updateItem(1, updateDto);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Pensil 2B Updated", result.getName());
        assertEquals(3000.0, result.getPrice());
        verify(itemRepository, times(1)).findById(1);
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void updateItem_NotFound() {
        // Given
        ItemDto updateDto = ItemDto.builder()
                .id(99)
                .name("Non-existent Item")
                .price(1000.0)
                .build();

        when(itemRepository.findById(99)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> itemService.updateItem(99, updateDto));
        verify(itemRepository, times(1)).findById(99);
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void deleteItem_Success() {
        // Given
        when(itemRepository.findById(1)).thenReturn(Optional.of(item1));
        when(itemRepository.hasInventories(1)).thenReturn(false);
        doNothing().when(itemRepository).delete(any(Item.class));

        // When
        itemService.deleteItem(1);

        // Then
        verify(itemRepository, times(1)).findById(1);
        verify(itemRepository, times(1)).hasInventories(1);
        verify(itemRepository, times(1)).delete(any(Item.class));
    }

    @Test
    void deleteItem_HasInventory() {
        // Given
        when(itemRepository.findById(1)).thenReturn(Optional.of(item1));
        when(itemRepository.hasInventories(1)).thenReturn(true);
        doThrow(new BusinessLogicException("Cannot delete item. Item has inventory entries. Delete inventory first."))
                .when(validationUtil).validateCondition(false, "Cannot delete item. Item has inventory entries. Delete inventory first.");

        // When & Then
        assertThrows(BusinessLogicException.class, () -> itemService.deleteItem(1));
        verify(itemRepository, times(1)).findById(1);
        verify(itemRepository, times(1)).hasInventories(1);
        verify(itemRepository, never()).delete(any(Item.class));
    }

    @Test
    void deleteItem_NotFound() {
        // Given
        when(itemRepository.findById(99)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> itemService.deleteItem(99));
        verify(itemRepository, times(1)).findById(99);
        verify(itemRepository, never()).hasInventories(anyInt());
        verify(itemRepository, never()).delete(any(Item.class));
    }
}