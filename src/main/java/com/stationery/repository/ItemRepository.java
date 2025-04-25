package com.stationery.repository;

import com.stationery.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {

    // Periksa relasi item dengan inventory
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM Item item JOIN item.inventories i WHERE item.id = :id")
    boolean hasInventories(@Param("id") Integer id);
}