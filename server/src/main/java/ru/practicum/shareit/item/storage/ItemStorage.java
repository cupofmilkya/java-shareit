package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {

    Item postItem(Item item);

    Item updateItem(long id, long itemId, Item item);

    Item getItem(long id);

    List<Item> getItems(long id);

    List<Item> getItemsByText(String text);
}
