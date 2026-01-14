package ru.practicum.shareit.item.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.handler.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class ItemStorageImpl implements ItemStorage {

    private final Map<Long, Item> items;

    @Override
    public Item postItem(Item item) {
        item.setId(generateId());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item updateItem(long id, long itemId, Item patch) {
        Item item = items.get(itemId);

        if (item == null) {
            throw new NotFoundException("Предмет с id " + itemId + " не найден");
        }

        if (item.getOwner().getId() != id) {
            throw new NotFoundException("Предмет с id " + itemId + " не принадлежит пользователю с id " + id);
        }

        if (patch.getName() != null) {
            if (patch.getName().isBlank()) {
                throw new IllegalArgumentException("Поле name обязательно");
            }

            item.setName(patch.getName());
        }
        if (patch.getDescription() != null) {
            if (patch.getDescription().isBlank()) {
                throw new IllegalArgumentException("Поле description обязательно");
            }

            item.setDescription(patch.getDescription());
        }
        if (patch.getAvailable() != null) {
            item.setAvailable(patch.getAvailable());
        }

        return item;
    }

    @Override
    public Item getItem(long id) {
        return items.get(id);
    }

    @Override
    public List<Item> getItems(long id) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId() == id)
                .toList();
    }

    @Override
    public List<Item> getItemsByText(String text) {
        String lowerText = text.toLowerCase();

        return items.values().stream()
                .filter(item -> item.getName().toLowerCase().contains(lowerText) ||
                        item.getDescription().toLowerCase().contains(lowerText))
                .distinct()
                .filter(item -> item.getAvailable() == true)
                .toList();
    }

    private long generateId() {
        return items.size() + 1;
    }
}
