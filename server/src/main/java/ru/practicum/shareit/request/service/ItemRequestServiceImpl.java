package ru.practicum.shareit.request.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.handler.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestResponseDto postItemRequest(ItemRequestDto dto, long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        ItemRequest item = ItemRequestMapper.toEntity(dto);
        item.setRequestor(user);
        item.setCreated(LocalDateTime.now());

        ItemRequest savedItemRequest = itemRequestRepository.save(item);
        log.info("Создан ItemRequest:{}", savedItemRequest);

        return ItemRequestMapper.toDto(savedItemRequest);
    }

    @Override
    public List<ItemRequestResponseDto> getItemRequests(long userId) {
        List<ItemRequest> itemRequests = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId);

        for (ItemRequest req : itemRequests) {
            List<Item> items = itemRepository.findByRequestId(req.getId());
            req.setItems(items);
        }

        return itemRequests.stream()
                .map(ItemRequestMapper::toDto)
                .toList();
    }

    @Override
    public List<ItemRequestResponseDto> getAllItemRequests() {
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByOrderByCreatedDesc();

        for (ItemRequest req : itemRequests) {
            List<Item> items = itemRepository.findByRequestId(req.getId());
            req.setItems(items);
        }

        return itemRequests.stream()
                .map(ItemRequestMapper::toDto)
                .toList();
    }

    @Override
    public ItemRequestResponseDto getItemRequest(long id) {
        ItemRequest itemRequest = itemRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Запрос с id " + id + " не найден"));

        List<Item> items = itemRepository.findByRequestId(itemRequest.getId());

        itemRequest.setItems(items);

        return ItemRequestMapper.toDto(itemRequest);
    }
}
