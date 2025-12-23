package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.handler.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentDtoMapper;
import ru.practicum.shareit.item.mapper.ItemDtoMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public ItemDto postItem(long userId, ItemDto itemDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        Item item = ItemDtoMapper.toModel(itemDto);
        item.setOwner(user);

        Item savedItem = itemRepository.save(item);
        log.info("Создан Item:{}", savedItem);

        return ItemDto.builder()
                .id(savedItem.getId())
                .name(savedItem.getName())
                .description(savedItem.getDescription())
                .available(savedItem.getAvailable())
                .owner(savedItem.getOwner())
                .request(savedItem.getRequest() != null ?
                        ItemRequestMapper.toDto(savedItem.getRequest()) : null)
                .lastBooking(null)
                .nextBooking(null)
                .comments(Collections.emptyList())
                .build();
    }

    @Override
    public ItemDto updateItem(long userId, long itemId, ItemDto itemDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с id " + itemId + " не найден"));

        if (item.getOwner().getId() != userId) {
            throw new NotFoundException("Предмет с id " + itemId + " не принадлежит пользователю с id " + userId);
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(item);
        log.info("Обновлен Item:{}", updatedItem);

        List<Comment> comments = commentRepository.findByItemIdOrderByCreatedDesc(itemId);
        List<CommentDto> commentDtos = comments.stream()
                .map(CommentDtoMapper::toDto)
                .collect(Collectors.toList());

        BookingForItemDto lastBooking = getLastBookingForItem(itemId, userId);
        BookingForItemDto nextBooking = getNextBookingForItem(itemId, userId);

        return ItemDto.builder()
                .id(updatedItem.getId())
                .name(updatedItem.getName())
                .description(updatedItem.getDescription())
                .available(updatedItem.getAvailable())
                .owner(updatedItem.getOwner())
                .request(updatedItem.getRequest() != null ?
                        ItemRequestMapper.toDto(updatedItem.getRequest()) : null)
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(commentDtos)
                .build();
    }

    @Override
    public ItemDto getItem(long itemId) {
        return getItem(itemId, null);
    }

    public ItemDto getItem(long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с id " + itemId + " не найден"));

        List<Comment> comments = commentRepository.findByItemIdOrderByCreatedDesc(itemId);
        List<CommentDto> commentDtos = comments.stream()
                .map(CommentDtoMapper::toDto)
                .collect(Collectors.toList());

        BookingForItemDto lastBooking = null;
        BookingForItemDto nextBooking = null;

        if (item.getOwner() != null && item.getOwner().getId().equals(userId)) {
            lastBooking = getLastBookingForItem(itemId, userId);
            nextBooking = getNextBookingForItem(itemId, userId);
        }

        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .owner(item.getOwner())
                .request(item.getRequest() != null ?
                        ItemRequestMapper.toDto(item.getRequest()) : null)
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(commentDtos)
                .build();
    }

    @Override
    public List<ItemDto> getItems(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        List<Item> items = itemRepository.findAllByOwnerId(userId);

        return items.stream().map(item -> {
            List<Comment> comments = commentRepository.findByItemIdOrderByCreatedDesc(item.getId());
            List<CommentDto> commentDtos = comments.stream()
                    .map(CommentDtoMapper::toDto)
                    .collect(Collectors.toList());

            BookingForItemDto lastBooking = getLastBookingForItem(item.getId(), userId);
            BookingForItemDto nextBooking = getNextBookingForItem(item.getId(), userId);

            return ItemDto.builder()
                    .id(item.getId())
                    .name(item.getName())
                    .description(item.getDescription())
                    .available(item.getAvailable())
                    .owner(item.getOwner())
                    .request(item.getRequest() != null ?
                            ItemRequestMapper.toDto(item.getRequest()) : null)
                    .lastBooking(lastBooking)
                    .nextBooking(nextBooking)
                    .comments(commentDtos)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> getItemsByText(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }

        List<Item> items = itemRepository.searchAvailableItems(text.toLowerCase());

        return items.stream().map(item -> {
            List<Comment> comments = commentRepository.findByItemIdOrderByCreatedDesc(item.getId());
            List<CommentDto> commentDtos = comments.stream()
                    .map(CommentDtoMapper::toDto)
                    .collect(Collectors.toList());

            return ItemDto.builder()
                    .id(item.getId())
                    .name(item.getName())
                    .description(item.getDescription())
                    .available(item.getAvailable())
                    .owner(item.getOwner())
                    .request(item.getRequest() != null ?
                            ItemRequestMapper.toDto(item.getRequest()) : null)
                    .lastBooking(null)
                    .nextBooking(null)
                    .comments(commentDtos)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public CommentDto postComment(long userId, long itemId, CommentRequestDto commentRequestDto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с id " + itemId + " не найден"));

        List<Booking> bookings = bookingRepository.findBookingsForComment(
                itemId,
                userId,
                BookingStatus.APPROVED,
                LocalDateTime.now());

        if (bookings.isEmpty()) {
            throw new IllegalArgumentException("Пользователь с id " + userId +
                    " не брал в аренду вещь с id " + itemId + " или аренда еще не завершена");
        }

        if (commentRequestDto.getText() == null || commentRequestDto.getText().isBlank()) {
            throw new IllegalArgumentException("Текст комментария не может быть пустым");
        }

        Comment comment = Comment.builder()
                .text(commentRequestDto.getText())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();

        Comment savedComment = commentRepository.save(comment);
        log.info("Создан комментарий: {}", savedComment);

        return CommentDtoMapper.toDto(savedComment);
    }

    private BookingForItemDto getLastBookingForItem(Long itemId, Long ownerId) {
        try {
            if (ownerId == null) return null;

            Item item = itemRepository.findById(itemId).orElse(null);
            if (item == null || item.getOwner() == null || !item.getOwner().getId().equals(ownerId)) {
                return null;
            }

            List<Booking> bookings = bookingRepository.findByItemIdAndStatusAndEndBeforeOrderByEndDesc(
                    itemId, BookingStatus.APPROVED, LocalDateTime.now());

            if (!bookings.isEmpty()) {
                Booking booking = bookings.getFirst();
                return BookingForItemDto.builder()
                        .id(booking.getId())
                        .start(booking.getStart())
                        .end(booking.getEnd())
                        .bookerId(booking.getBooker().getId())
                        .status(booking.getStatus())
                        .build();
            }
        } catch (Exception e) {
            log.warn("Ошибка при получении lastBooking для itemId {}: {}", itemId, e.getMessage());
        }
        return null;
    }

    private BookingForItemDto getNextBookingForItem(Long itemId, Long ownerId) {
        try {
            if (ownerId == null) return null;

            Item item = itemRepository.findById(itemId).orElse(null);
            if (item == null || item.getOwner() == null || !item.getOwner().getId().equals(ownerId)) {
                return null;
            }

            List<Booking> bookings = bookingRepository.findByItemIdAndStatusAndStartAfterOrderByStartAsc(
                    itemId, BookingStatus.APPROVED, LocalDateTime.now());

            if (!bookings.isEmpty()) {
                Booking booking = bookings.getFirst();
                return BookingForItemDto.builder()
                        .id(booking.getId())
                        .start(booking.getStart())
                        .end(booking.getEnd())
                        .bookerId(booking.getBooker().getId())
                        .status(booking.getStatus())
                        .build();
            }
        } catch (Exception e) {
            log.warn("Ошибка при получении nextBooking для itemId {}: {}", itemId, e.getMessage());
        }
        return null;
    }
}