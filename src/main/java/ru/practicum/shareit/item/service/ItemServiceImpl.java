package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.handler.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemDtoMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    public ItemDto postItem(long id, ItemDto itemDto) {
        Item item = ItemDtoMapper.toModel(itemDto);
        User user = userStorage.getUser(id);
        if (user == null) {
            throw new NotFoundException("Пользователя с id " + id + " не существует");
        }
        item.setOwner(user);
        log.info("Создан Item:{}", item);

        return ItemDtoMapper.toDto(itemStorage.postItem(item));
    }

    @Override
    public ItemDto updateItem(long id, long itemId, ItemDto itemDto) {
        Item item = ItemDtoMapper.toModel(itemDto);
        log.info("Обновлен Item:{}", item);
        return ItemDtoMapper.toDto(itemStorage.updateItem(id, itemId, item));
    }

    @Override
    public ItemDto getItem(long id) {
        return ItemDtoMapper.toDto(itemStorage.getItem(id));
    }

    @Override
    public List<ItemDto> getItems(long id) {
        User user = userStorage.getUser(id);
        if (user == null) {
            throw new NotFoundException("Пользователя с id " + id + " не существует");
        }

        return itemStorage.getItems(id).stream()
                .map(ItemDtoMapper::toDto)
                .toList();
    }

    @Override
    public List<ItemDto> getItemsByText(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }

        return itemStorage.getItemsByText(text).stream()
                .map(ItemDtoMapper::toDto)
                .toList();
    }
}
