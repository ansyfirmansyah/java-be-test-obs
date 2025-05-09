package com.stationery.repository;

import com.stationery.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {

    List<Inventory> findByItemId(Integer itemId);

    Page<Inventory> findByItemId(Integer itemId, Pageable pageable);

    void deleteByOrderId(UUID orderId);

    /**
     * Calculate the remaining stock quantity for an item
     */
    @Query("SELECT COALESCE(SUM(CASE WHEN i.type = 'T' THEN i.qty ELSE -i.qty END), 0) " +
            "FROM Inventory i WHERE i.item.id = :itemId")
    Integer calculateRemainingStock(@Param("itemId") Integer itemId);
}