package com.stationery.controller;

import com.stationery.dto.OrderDto;
import com.stationery.dto.response.ApiResponse;
import com.stationery.dto.response.PageResponse;
import com.stationery.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDto>> getOrder(@PathVariable UUID id) {
        log.info("Request to get order with ID: {}", id);
        OrderDto order = orderService.getOrder(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Order retrieved successfully", order));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderDto>>> getAllOrders(Pageable pageable) {
        log.info("Request to get all orders, page: {}", pageable.getPageNumber());
        Page<OrderDto> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Orders retrieved successfully",
                PageResponse.fromPage(orders)));
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<ApiResponse<PageResponse<OrderDto>>> getOrdersByItemId(
            @PathVariable Integer itemId, Pageable pageable) {
        log.info("Request to get orders for item ID: {}, page: {}", itemId, pageable.getPageNumber());
        Page<OrderDto> orders = orderService.getOrdersByItemId(itemId, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Orders retrieved successfully",
                PageResponse.fromPage(orders)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDto>> createOrder(@Valid @RequestBody OrderDto orderDto) {
        log.info("Request to create order: {}", orderDto);
        OrderDto createdOrder = orderService.createOrder(orderDto);
        return new ResponseEntity<>(new ApiResponse<>(true, "Order created successfully", createdOrder),
                HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDto>> updateOrder(
            @PathVariable UUID id, @Valid @RequestBody OrderDto orderDto) {
        log.info("Request to update order with ID: {}", id);
        OrderDto updatedOrder = orderService.updateOrder(id, orderDto);
        return ResponseEntity.ok(new ApiResponse<>(true, "Order updated successfully", updatedOrder));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(@PathVariable UUID id) {
        log.info("Request to delete order with ID: {}", id);
        orderService.deleteOrder(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Order deleted successfully", null));
    }
}