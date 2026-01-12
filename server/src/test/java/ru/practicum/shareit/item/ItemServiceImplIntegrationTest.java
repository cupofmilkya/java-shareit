package ru.practicum.shareit.item;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.handler.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemServiceImpl itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private User testOwner;
    private User testUser;
    private User testBooker;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        itemRepository.deleteAll();
        commentRepository.deleteAll();
        bookingRepository.deleteAll();
        itemRequestRepository.deleteAll();

        testOwner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@example.com")
                .build());

        testUser = userRepository.save(User.builder()
                .name("Test User")
                .email("user@example.com")
                .build());

        testBooker = userRepository.save(User.builder()
                .name("Booker")
                .email("booker@example.com")
                .build());
    }

    private ItemDto createTestItemDto() {
        return ItemDto.builder()
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();
    }

    @Test
    @DisplayName("Создание предмета")
    void testPostItem() {
        ItemDto itemDto = createTestItemDto();
        ItemDto savedItem = itemService.postItem(testOwner.getId(), itemDto);

        assertThat(savedItem).isNotNull();
        assertThat(savedItem.getId()).isNotNull();
        assertThat(savedItem.getName()).isEqualTo("Test Item");
        assertThat(savedItem.getDescription()).isEqualTo("Test Description");
        assertThat(savedItem.getAvailable()).isTrue();
        assertThat(savedItem.getOwner().getId()).isEqualTo(testOwner.getId());
    }

    @Test
    @DisplayName("Создание предмета с несуществующим пользователем")
    void testPostItemWithNonExistentUser() {
        ItemDto itemDto = createTestItemDto();

        assertThrows(NotFoundException.class,
                () -> itemService.postItem(999L, itemDto));
    }

    @Test
    @DisplayName("Создание предмета с requestId")
    void testPostItemWithRequestId() {
        ItemRequest request = itemRequestRepository.save(ItemRequest.builder()
                .description("Need a drill")
                .requestor(testUser)
                .created(LocalDateTime.now())
                .build());

        ItemDto itemDto = ItemDto.builder()
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .requestId(request.getId())
                .build();

        ItemDto savedItem = itemService.postItem(testOwner.getId(), itemDto);

        assertThat(savedItem).isNotNull();
        assertThat(savedItem.getRequest()).isNotNull();
        assertThat(savedItem.getRequest().getId()).isEqualTo(request.getId());
    }

    @Test
    @DisplayName("Обновление предмета владельцем")
    void testUpdateItemByOwner() {
        ItemDto itemDto = createTestItemDto();
        ItemDto savedItem = itemService.postItem(testOwner.getId(), itemDto);

        ItemDto updateDto = ItemDto.builder()
                .name("Updated Item")
                .description("Updated Description")
                .available(false)
                .build();

        ItemDto updatedItem = itemService.updateItem(testOwner.getId(), savedItem.getId(), updateDto);

        assertThat(updatedItem).isNotNull();
        assertThat(updatedItem.getId()).isEqualTo(savedItem.getId());
        assertThat(updatedItem.getName()).isEqualTo("Updated Item");
        assertThat(updatedItem.getDescription()).isEqualTo("Updated Description");
        assertThat(updatedItem.getAvailable()).isFalse();
    }

    @Test
    @DisplayName("Обновление предмета не владельцем")
    void testUpdateItemByNonOwner() {
        ItemDto itemDto = createTestItemDto();
        ItemDto savedItem = itemService.postItem(testOwner.getId(), itemDto);

        ItemDto updateDto = ItemDto.builder()
                .name("Hacked Item")
                .build();

        assertThrows(NotFoundException.class,
                () -> itemService.updateItem(testUser.getId(), savedItem.getId(), updateDto));
    }

    @Test
    @DisplayName("Частичное обновление предмета")
    void testPartialUpdateItem() {
        ItemDto itemDto = createTestItemDto();
        ItemDto savedItem = itemService.postItem(testOwner.getId(), itemDto);

        ItemDto updateDto = ItemDto.builder()
                .name("Updated Name")
                .build();

        ItemDto updatedItem = itemService.updateItem(testOwner.getId(), savedItem.getId(), updateDto);

        assertThat(updatedItem).isNotNull();
        assertThat(updatedItem.getName()).isEqualTo("Updated Name");
        assertThat(updatedItem.getDescription()).isEqualTo("Test Description");
        assertThat(updatedItem.getAvailable()).isTrue();
    }

    @Test
    @DisplayName("Получение предмета владельцем")
    void testGetItemByOwner() {
        ItemDto itemDto = createTestItemDto();
        ItemDto savedItem = itemService.postItem(testOwner.getId(), itemDto);

        ItemDto retrievedItem = itemService.getItem(savedItem.getId(), testOwner.getId());

        assertThat(retrievedItem).isNotNull();
        assertThat(retrievedItem.getId()).isEqualTo(savedItem.getId());
        assertThat(retrievedItem.getLastBooking()).isNull();
        assertThat(retrievedItem.getNextBooking()).isNull();
        assertThat(retrievedItem.getComments()).isEmpty();
    }

    @Test
    @DisplayName("Получение предмета не владельцем")
    void testGetItemByNonOwner() {
        ItemDto itemDto = createTestItemDto();
        ItemDto savedItem = itemService.postItem(testOwner.getId(), itemDto);

        ItemDto retrievedItem = itemService.getItem(savedItem.getId(), testUser.getId());

        assertThat(retrievedItem).isNotNull();
        assertThat(retrievedItem.getId()).isEqualTo(savedItem.getId());
        assertThat(retrievedItem.getLastBooking()).isNull();
        assertThat(retrievedItem.getNextBooking()).isNull();
    }

    @Test
    @DisplayName("Получение несуществующего предмета")
    void testGetNonExistentItem() {
        assertThrows(NotFoundException.class,
                () -> itemService.getItem(999L, testOwner.getId()));
    }

    @Test
    @DisplayName("Получение всех предметов пользователя")
    void testGetItems() {
        ItemDto item1 = itemService.postItem(testOwner.getId(),
                ItemDto.builder().name("Item 1").description("Desc 1").available(true).build());

        ItemDto item2 = itemService.postItem(testOwner.getId(),
                ItemDto.builder().name("Item 2").description("Desc 2").available(true).build());

        List<ItemDto> items = itemService.getItems(testOwner.getId());

        assertThat(items).hasSize(2);
        assertThat(items).extracting(ItemDto::getId)
                .containsExactlyInAnyOrder(item1.getId(), item2.getId());
    }

    @Test
    @DisplayName("Получение предметов несуществующего пользователя")
    void testGetItemsForNonExistentUser() {
        assertThrows(NotFoundException.class,
                () -> itemService.getItems(999L));
    }

    @Test
    @DisplayName("Поиск доступных предметов по тексту")
    void testGetItemsByText() {
        ItemDto availableItem = itemService.postItem(testOwner.getId(),
                ItemDto.builder().name("Power drill").description("Cordless drill").available(true).build());

        itemService.postItem(testOwner.getId(),
                ItemDto.builder().name("Hammer").description("Steel hammer").available(false).build());

        List<ItemDto> foundItems = itemService.getItemsByText("drill");

        assertThat(foundItems).hasSize(1);
        assertThat(foundItems.getFirst().getId()).isEqualTo(availableItem.getId());
        assertThat(foundItems.getFirst().getName()).isEqualTo("Power drill");
    }

    @Test
    @DisplayName("Поиск по пустому тексту")
    void testGetItemsByEmptyText() {
        itemService.postItem(testOwner.getId(), createTestItemDto());

        List<ItemDto> foundItems = itemService.getItemsByText("");

        assertThat(foundItems).isEmpty();
    }

    @Test
    @DisplayName("Добавление комментария к предмету")
    void testPostComment() {
        ItemDto itemDto = createTestItemDto();
        ItemDto savedItem = itemService.postItem(testOwner.getId(), itemDto);

        Booking booking = bookingRepository.save(Booking.builder()
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .item(itemRepository.findById(savedItem.getId()).orElseThrow())
                .booker(testBooker)
                .status(BookingStatus.APPROVED)
                .build());

        CommentRequestDto commentRequest = CommentRequestDto.builder()
                .text("item")
                .build();

        CommentDto commentDto = itemService.postComment(testBooker.getId(), savedItem.getId(), commentRequest);

        assertThat(commentDto).isNotNull();
        assertThat(commentDto.getText()).isEqualTo("item");
        assertThat(commentDto.getAuthorName()).isEqualTo(testBooker.getName());
        assertThat(commentDto.getCreated()).isNotNull();
    }

    @Test
    @DisplayName("Добавление комментария без бронирования")
    void testPostCommentWithoutBooking() {
        ItemDto itemDto = createTestItemDto();
        ItemDto savedItem = itemService.postItem(testOwner.getId(), itemDto);

        CommentRequestDto commentRequest = CommentRequestDto.builder()
                .text("item")
                .build();

        assertThrows(ValidationException.class,
                () -> itemService.postComment(testUser.getId(), savedItem.getId(), commentRequest));
    }

    @Test
    @DisplayName("Добавление комментария с пустым текстом")
    void testPostCommentWithEmptyText() {
        ItemDto itemDto = createTestItemDto();
        ItemDto savedItem = itemService.postItem(testOwner.getId(), itemDto);

        CommentRequestDto commentRequest = CommentRequestDto.builder()
                .text("")
                .build();

        assertThrows(ValidationException.class,
                () -> itemService.postComment(testBooker.getId(), savedItem.getId(), commentRequest));
    }

    @Test
    @DisplayName("Получение предмета с комментариями")
    void testGetItemWithComments() {
        ItemDto itemDto = createTestItemDto();
        ItemDto savedItem = itemService.postItem(testOwner.getId(), itemDto);

        Booking booking = bookingRepository.save(Booking.builder()
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .item(itemRepository.findById(savedItem.getId()).orElseThrow())
                .booker(testBooker)
                .status(BookingStatus.APPROVED)
                .build());

        CommentRequestDto commentRequest = CommentRequestDto.builder()
                .text("item")
                .build();
        itemService.postComment(testBooker.getId(), savedItem.getId(), commentRequest);

        ItemDto retrievedItem = itemService.getItem(savedItem.getId(), testOwner.getId());

        assertThat(retrievedItem).isNotNull();
        assertThat(retrievedItem.getComments()).hasSize(1);
        assertThat(retrievedItem.getComments().getFirst().getText()).isEqualTo("item");
    }
}