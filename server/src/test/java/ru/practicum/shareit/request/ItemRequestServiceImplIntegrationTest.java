package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.handler.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestServiceImpl itemRequestService;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        itemRepository.deleteAll();
        itemRequestRepository.deleteAll();

        testUser1 = userRepository.save(User.builder()
                .name("User 1")
                .email("user1@example.com")
                .build());

        testUser2 = userRepository.save(User.builder()
                .name("User 2")
                .email("user2@example.com")
                .build());
    }

    @Test
    @DisplayName("Создание запроса на предмет")
    void testPostItemRequest() {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a drill for home repairs")
                .build();

        ItemRequestResponseDto savedRequest = itemRequestService.postItemRequest(requestDto, testUser1.getId());

        assertThat(savedRequest).isNotNull();
        assertThat(savedRequest.getId()).isNotNull();
        assertThat(savedRequest.getDescription()).isEqualTo("Need a drill for home repairs");
        assertThat(savedRequest.getRequestorId()).isEqualTo(testUser1.getId());
        assertThat(savedRequest.getCreated()).isNotNull();
        assertThat(savedRequest.getItems()).isEmpty();
    }

    @Test
    @DisplayName("Создание запроса с несуществующим пользователем")
    void testPostItemRequestWithNonExistentUser() {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a drill")
                .build();

        assertThrows(NotFoundException.class,
                () -> itemRequestService.postItemRequest(requestDto, 999L));
    }

    @Test
    @DisplayName("Получение запросов пользователя")
    void testGetItemRequests() {
        itemRequestService.postItemRequest(
                ItemRequestDto.builder().description("Need a drill").build(), testUser1.getId());
        itemRequestService.postItemRequest(
                ItemRequestDto.builder().description("Need a hammer").build(), testUser1.getId());

        List<ItemRequestResponseDto> requests = itemRequestService.getItemRequests(testUser1.getId());

        assertThat(requests).hasSize(2);
    }

    @Test
    @DisplayName("Получение всех запросов")
    void testGetAllItemRequests() {
        itemRequestService.postItemRequest(
                ItemRequestDto.builder().description("Request from user 1").build(), testUser1.getId());
        itemRequestService.postItemRequest(
                ItemRequestDto.builder().description("Request from user 2").build(), testUser2.getId());

        List<ItemRequestResponseDto> allRequests = itemRequestService.getAllItemRequests();

        assertThat(allRequests).hasSize(2);
    }

    @Test
    @DisplayName("Получение запроса по ID")
    void testGetItemRequest() {
        ItemRequestResponseDto savedRequest = itemRequestService.postItemRequest(
                ItemRequestDto.builder().description("Need a drill").build(), testUser1.getId());

        ItemRequestResponseDto retrievedRequest = itemRequestService.getItemRequest(savedRequest.getId());

        assertThat(retrievedRequest).isNotNull();
        assertThat(retrievedRequest.getId()).isEqualTo(savedRequest.getId());
        assertThat(retrievedRequest.getDescription()).isEqualTo("Need a drill");
    }

    @Test
    @DisplayName("Получение запроса с предметами")
    void testGetItemRequestWithItems() {
        ItemRequestResponseDto savedRequest = itemRequestService.postItemRequest(
                ItemRequestDto.builder().description("Need a drill").build(), testUser1.getId());

        Item item = Item.builder()
                .name("Power Drill")
                .description("Cordless drill")
                .available(true)
                .owner(testUser2)
                .request(itemRequestRepository.findById(savedRequest.getId()).orElseThrow())
                .build();
        itemRepository.save(item);

        ItemRequestResponseDto retrievedRequest = itemRequestService.getItemRequest(savedRequest.getId());

        assertThat(retrievedRequest).isNotNull();
        assertThat(retrievedRequest.getItems()).hasSize(1);
        assertThat(retrievedRequest.getItems().getFirst().getName()).isEqualTo("Power Drill");
    }

    @Test
    @DisplayName("Получение несуществующего запроса")
    void testGetNonExistentItemRequest() {
        assertThrows(NotFoundException.class,
                () -> itemRequestService.getItemRequest(999L));
    }
}