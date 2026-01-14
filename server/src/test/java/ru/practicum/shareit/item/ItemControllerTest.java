package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserDtoMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private ItemDto testItemDto;
    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        testUserDto = UserDto.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();

        testItemDto = ItemDto.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .owner(UserDtoMapper.toModel(testUserDto))
                .build();
    }

    @Test
    @DisplayName("Создание предмета")
    void testPostItem() throws Exception {
        when(itemService.postItem(eq(1L), any(ItemDto.class))).thenReturn(testItemDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testItemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Item"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.available").value(true));

        verify(itemService, times(1)).postItem(eq(1L), any(ItemDto.class));
    }

    @Test
    @DisplayName("Создание предмета без заголовка пользователя")
    void testPostItemWithoutUserId() throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testItemDto)))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).postItem(anyLong(), any(ItemDto.class));
    }

    @Test
    @DisplayName("Обновление предмета")
    void testUpdateItem() throws Exception {
        ItemDto updatedItemDto = ItemDto.builder()
                .id(1L)
                .name("Updated Item")
                .description("Updated Description")
                .available(false)
                .owner(UserDtoMapper.toModel(testUserDto))
                .build();

        when(itemService.updateItem(eq(1L), eq(1L), any(ItemDto.class))).thenReturn(updatedItemDto);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedItemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Item"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.available").value(false));

        verify(itemService, times(1)).updateItem(eq(1L), eq(1L), any(ItemDto.class));
    }

    @Test
    @DisplayName("Получение предмета владельцем")
    void testGetItemByOwner() throws Exception {
        when(itemService.getItem(eq(1L), eq(1L))).thenReturn(testItemDto);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Item"));

        verify(itemService, times(1)).getItem(1L, 1L);
    }

    @Test
    @DisplayName("Получение предмета без заголовка пользователя")
    void testGetItemWithoutUserId() throws Exception {
        when(itemService.getItem(eq(1L), isNull())).thenReturn(testItemDto);

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(itemService, times(1)).getItem(1L, null);
    }

    @Test
    @DisplayName("Получение несуществующего предмета")
    void testGetNonExistentItem() throws Exception {
        when(itemService.getItem(eq(999L), any()))
                .thenThrow(new ru.practicum.shareit.handler.exception.NotFoundException("Предмет не найден"));

        mockMvc.perform(get("/items/999")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound());

        verify(itemService, times(1)).getItem(999L, 1L);
    }

    @Test
    @DisplayName("Получение всех предметов пользователя")
    void testGetItems() throws Exception {
        List<ItemDto> items = List.of(testItemDto);
        when(itemService.getItems(1L)).thenReturn(items);

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Item"));

        verify(itemService, times(1)).getItems(1L);
    }

    @Test
    @DisplayName("Получение предметов без заголовка пользователя")
    void testGetItemsWithoutUserId() throws Exception {
        mockMvc.perform(get("/items"))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).getItems(anyLong());
    }

    @Test
    @DisplayName("Поиск предметов по тексту")
    void testGetItemsByText() throws Exception {
        List<ItemDto> items = List.of(testItemDto);
        when(itemService.getItemsByText("test")).thenReturn(items);

        mockMvc.perform(get("/items/search")
                        .param("text", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(itemService, times(1)).getItemsByText("test");
    }

    @Test
    @DisplayName("Поиск предметов без параметра text")
    void testGetItemsByTextWithoutParam() throws Exception {
        mockMvc.perform(get("/items/search"))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).getItemsByText(anyString());
    }

    @Test
    @DisplayName("Добавление комментария")
    void testPostComment() throws Exception {
        CommentRequestDto commentRequest = CommentRequestDto.builder()
                .text("Great item!")
                .build();

        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("Great item!")
                .authorName("Booker")
                .created(LocalDateTime.now())
                .build();

        when(itemService.postComment(eq(1L), eq(1L), any(CommentRequestDto.class)))
                .thenReturn(commentDto);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("Great item!"))
                .andExpect(jsonPath("$.authorName").value("Booker"));

        verify(itemService, times(1)).postComment(1L, 1L, commentRequest);
    }

    @Test
    @DisplayName("Добавление комментария без заголовка пользователя")
    void testPostCommentWithoutUserId() throws Exception {
        CommentRequestDto commentRequest = CommentRequestDto.builder()
                .text("Great item!")
                .build();

        mockMvc.perform(post("/items/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).postComment(anyLong(), anyLong(), any(CommentRequestDto.class));
    }
}