package com.stationery.repository;

import com.stationery.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {

    Optional<Item> findByName(String name);

    /**
     * Custom query to get an item with its remaining stock count
     */
    @Query("SELECT i, " +
            "(SELECT COALESCE(SUM(CASE WHEN inv.type = 'T' THEN inv.qty ELSE -inv.qty END), 0) " +
            "FROM Inventory inv WHERE inv.item.id = i.id) AS stockCount " +
            "FROM Item i WHERE i.id = :itemId")
    Optional<Object[]> findItemWithStockCount(@Param("itemId") Integer itemId);

    /**
     * Custom query to get all items with their remaining stock counts
     */
    @Query("SELECT i, " +
            "(SELECT COALESCE(SUM(CASE WHEN inv.type = 'T' THEN inv.qty ELSE -inv.qty END), 0) " +
            "FROM Inventory inv WHERE inv.item.id = i.id) AS stockCount " +
            "FROM Item i")
    List<Object[]> findAllItemsWithStockCount();

    // Check if the item has inventory entries - updated for Spring Boot 3
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM Item item JOIN item.inventories i WHERE item.id = :id")
    boolean hasInventories(@Param("id") Integer id);
}