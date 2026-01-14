package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import shareit.ShareItGateway;
import shareit.item.ItemClient;
import shareit.item.ItemController;
import shareit.item.dto.CommentRequestDto;
import shareit.item.dto.ItemDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
@ContextConfiguration(classes = {ShareItGateway.class, ItemController.class})
class ItemGatewayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClient itemClient;

    @Test
    @DisplayName("Создание вещи с пустым именем")
    void testPostItemWithEmptyName() throws Exception {
        ItemDto invalidDto = ItemDto.builder()
                .name("")
                .description("Valid Description")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).postItem(any(Long.class), any(ItemDto.class));
    }

    @Test
    @DisplayName("Создание вещи с null именем")
    void testPostItemWithNullName() throws Exception {
        ItemDto invalidDto = ItemDto.builder()
                .description("Valid Description")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).postItem(any(Long.class), any(ItemDto.class));
    }

    @Test
    @DisplayName("Создание вещи с пустым описанием")
    void testPostItemWithEmptyDescription() throws Exception {
        ItemDto invalidDto = ItemDto.builder()
                .name("Valid Name")
                .description("")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).postItem(any(Long.class), any(ItemDto.class));
    }

    @Test
    @DisplayName("Создание вещи с null описанием")
    void testPostItemWithNullDescription() throws Exception {
        ItemDto invalidDto = ItemDto.builder()
                .name("Valid Name")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).postItem(any(Long.class), any(ItemDto.class));
    }

    @Test
    @DisplayName("Создание вещи с null available")
    void testPostItemWithNullAvailable() throws Exception {
        ItemDto invalidDto = ItemDto.builder()
                .name("Valid Name")
                .description("Valid Description")
                .build();

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).postItem(any(Long.class), any(ItemDto.class));
    }

    @Test
    @DisplayName("Создание вещи с валидными данными")
    void testPostItemWithValidData() throws Exception {
        ItemDto validDto = ItemDto.builder()
                .name("Valid Name")
                .description("Valid Description")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isOk());

        verify(itemClient).postItem(eq(1L), any(ItemDto.class));
    }

    @Test
    @DisplayName("Обновление вещи с пустым именем")
    void testUpdateItemWithEmptyName() throws Exception {
        ItemDto invalidDto = ItemDto.builder()
                .name("")
                .build();

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isOk());

        verify(itemClient).updateItem(eq(1L), eq(1L), any(ItemDto.class));
    }

    @Test
    @DisplayName("Обновление вещи только с описанием")
    void testUpdateItemWithDescriptionOnly() throws Exception {
        ItemDto validDto = ItemDto.builder()
                .description("Updated Description")
                .build();

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isOk());

        verify(itemClient).updateItem(eq(1L), eq(1L), any(ItemDto.class));
    }

    @Test
    @DisplayName("Обновление вещи только с available")
    void testUpdateItemWithAvailableOnly() throws Exception {
        ItemDto validDto = ItemDto.builder()
                .available(false)
                .build();

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isOk());

        verify(itemClient).updateItem(eq(1L), eq(1L), any(ItemDto.class));
    }

    @Test
    @DisplayName("Создание комментария с пустым текстом")
    void testPostCommentWithEmptyText() throws Exception {
        CommentRequestDto invalidDto = CommentRequestDto.builder()
                .text("")
                .build();

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).postComment(any(Long.class), any(Long.class), any(CommentRequestDto.class));
    }

    @Test
    @DisplayName("Создание комментария с null текстом")
    void testPostCommentWithNullText() throws Exception {
        CommentRequestDto invalidDto = CommentRequestDto.builder()
                .text(null)
                .build();

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).postComment(any(Long.class), any(Long.class), any(CommentRequestDto.class));
    }

    @Test
    @DisplayName("Создание комментария с пробелами в тексте")
    void testPostCommentWithBlankText() throws Exception {
        CommentRequestDto invalidDto = CommentRequestDto.builder()
                .text("   ")
                .build();

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).postComment(any(Long.class), any(Long.class), any(CommentRequestDto.class));
    }

    @Test
    @DisplayName("Создание комментария с очень длинным текстом")
    void testPostCommentWithTooLongText() throws Exception {
        String longText = "a".repeat(1001);
        CommentRequestDto invalidDto = CommentRequestDto.builder()
                .text(longText)
                .build();

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).postComment(any(Long.class), any(Long.class), any(CommentRequestDto.class));
    }

    @Test
    @DisplayName("Создание комментария с максимально допустимой длиной текста")
    void testPostCommentWithMaxLengthText() throws Exception {
        String maxText = "a".repeat(1000);
        CommentRequestDto validDto = CommentRequestDto.builder()
                .text(maxText)
                .build();

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isOk());

        verify(itemClient).postComment(eq(1L), eq(1L), any(CommentRequestDto.class));
    }

    @Test
    @DisplayName("Создание комментария с валидным текстом")
    void testPostCommentWithValidText() throws Exception {
        CommentRequestDto validDto = CommentRequestDto.builder()
                .text("Great item, thank you!")
                .build();

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isOk());

        verify(itemClient).postComment(eq(1L), eq(1L), any(CommentRequestDto.class));
    }

    @Test
    @DisplayName("Получение вещи без заголовка userId")
    void testGetItemWithoutUserIdHeader() throws Exception {
        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk());

        verify(itemClient).getItem(eq(null), eq(1L));
    }

    @Test
    @DisplayName("Получение вещи с заголовком userId")
    void testGetItemWithUserIdHeader() throws Exception {
        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk());

        verify(itemClient).getItem(eq(1L), eq(1L));
    }

    @Test
    @DisplayName("Получение вещей пользователя без заголовка")
    void testGetItemsWithoutHeader() throws Exception {
        mockMvc.perform(get("/items"))
                .andExpect(status().isInternalServerError());

        verify(itemClient, never()).getItems(any(Long.class));
    }

    @Test
    @DisplayName("Получение вещей пользователя с заголовком")
    void testGetItemsWithHeader() throws Exception {
        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk());

        verify(itemClient).getItems(1L);
    }

    @Test
    @DisplayName("Поиск вещей по тексту")
    void testSearchItemsByText() throws Exception {
        mockMvc.perform(get("/items/search?text=дрель"))
                .andExpect(status().isOk());

        verify(itemClient).getItemsByText("дрель");
    }

    @Test
    @DisplayName("Поиск вещей по пустому тексту")
    void testSearchItemsByEmptyText() throws Exception {
        mockMvc.perform(get("/items/search?text="))
                .andExpect(status().isOk());

        verify(itemClient).getItemsByText("");
    }

    @Test
    @DisplayName("Поиск вещей без параметра text")
    void testSearchItemsWithoutTextParam() throws Exception {
        mockMvc.perform(get("/items/search"))
                .andExpect(status().isInternalServerError());

        verify(itemClient, never()).getItemsByText(any(String.class));
    }

    @Test
    @DisplayName("Создание вещи без заголовка userId")
    void testPostItemWithoutHeader() throws Exception {
        ItemDto validDto = ItemDto.builder()
                .name("Valid Name")
                .description("Valid Description")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isInternalServerError());

        verify(itemClient, never()).postItem(any(Long.class), any(ItemDto.class));
    }

    @Test
    @DisplayName("Обновление вещи без заголовка userId")
    void testUpdateItemWithoutHeader() throws Exception {
        ItemDto validDto = ItemDto.builder()
                .name("Updated Name")
                .build();

        mockMvc.perform(patch("/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isInternalServerError());

        verify(itemClient, never()).updateItem(any(Long.class), any(Long.class), any(ItemDto.class));
    }

    @Test
    @DisplayName("Создание комментария без заголовка userId")
    void testPostCommentWithoutHeader() throws Exception {
        CommentRequestDto validDto = CommentRequestDto.builder()
                .text("Great item")
                .build();

        mockMvc.perform(post("/items/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isInternalServerError());

        verify(itemClient, never()).postComment(any(Long.class), any(Long.class), any(CommentRequestDto.class));
    }

    @Test
    @DisplayName("Обновление вещи со всеми полями")
    void testUpdateItemWithAllFields() throws Exception {
        ItemDto validDto = ItemDto.builder()
                .name("Updated Name")
                .description("Updated Description")
                .available(false)
                .build();

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isOk());

        verify(itemClient).updateItem(eq(1L), eq(1L), any(ItemDto.class));
    }

    @Test
    @DisplayName("Создание вещи с requestId")
    void testPostItemWithRequestId() throws Exception {
        ItemDto validDto = ItemDto.builder()
                .name("Item for Request")
                .description("Item created for a request")
                .available(true)
                .requestId(123L)
                .build();

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isOk());

        verify(itemClient).postItem(eq(1L), any(ItemDto.class));
    }
}