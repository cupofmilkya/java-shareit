package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerId(long ownerId);

    @Query("SELECT i FROM Item i WHERE i.available = true AND " +
            "(LOWER(i.name) LIKE %:text% OR LOWER(i.description) LIKE %:text%)")
    List<Item> searchAvailableItems(String text);
}