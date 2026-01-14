package ru.practicum.shareit.request;

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
import shareit.request.ItemRequestClient;
import shareit.request.ItemRequestController;
import shareit.request.dto.ItemRequestDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
@ContextConfiguration(classes = {ShareItGateway.class, ItemRequestController.class})
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestClient itemRequestClient;

    @Test
    @DisplayName("Создание запроса на вещь с пустым описанием")
    void testPostItemRequestWithEmptyDescription() throws Exception {
        ItemRequestDto invalidDto = ItemRequestDto.builder()
                .description("")
                .build();

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).postItemRequest(eq(1L), any(ItemRequestDto.class));
    }

    @Test
    @DisplayName("Создание запроса на вещь с null описанием")
    void testPostItemRequestWithNullDescription() throws Exception {
        ItemRequestDto invalidDto = ItemRequestDto.builder()
                .description(null)
                .build();

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).postItemRequest(eq(1L), any(ItemRequestDto.class));
    }

    @Test
    @DisplayName("Создание запроса на вещь с пробелами в описании")
    void testPostItemRequestWithBlankDescription() throws Exception {
        ItemRequestDto invalidDto = ItemRequestDto.builder()
                .description("   ")
                .build();

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(itemRequestClient, never()).postItemRequest(eq(1L), any(ItemRequestDto.class));
    }

    @Test
    @DisplayName("Создание запроса на вещь с валидным описанием")
    void testPostItemRequestWithValidDescription() throws Exception {
        ItemRequestDto validDto = ItemRequestDto.builder()
                .description("Нужна дрель для ремонта")
                .build();

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isOk());

        verify(itemRequestClient).postItemRequest(eq(1L), any(ItemRequestDto.class));
    }

    @Test
    @DisplayName("Создание запроса на вещь с длинным описанием")
    void testPostItemRequestWithLongDescription() throws Exception {
        ItemRequestDto validDto = ItemRequestDto.builder()
                .description("Нужна электрическая дрель мощностью не менее 800 Вт, с набором сверл по бетону и дереву, желательно с аккумулятором")
                .build();

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isOk());

        verify(itemRequestClient).postItemRequest(eq(1L), any(ItemRequestDto.class));
    }

    @Test
    @DisplayName("Создание запроса на вещь с некорректным userId в заголовке")
    void testPostItemRequestWithInvalidUserId() throws Exception {
        ItemRequestDto validDto = ItemRequestDto.builder()
                .description("Нужна дрель")
                .build();

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", "invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isInternalServerError());

        verify(itemRequestClient, never()).postItemRequest(any(Long.class), any(ItemRequestDto.class));
    }

    @Test
    @DisplayName("Получение запросов пользователя без заголовка X-Sharer-User-Id")
    void testGetItemRequestsWithoutUserIdHeader() throws Exception {
        mockMvc.perform(get("/requests"))
                .andExpect(status().isInternalServerError());

        verify(itemRequestClient, never()).getItemRequests(any(Long.class));
    }

    @Test
    @DisplayName("Получение конкретного запроса без заголовка X-Sharer-User-Id")
    void testGetItemRequestWithoutUserIdHeader() throws Exception {
        mockMvc.perform(get("/requests/1"))
                .andExpect(status().isInternalServerError());

        verify(itemRequestClient, never()).getItemRequest(any(Long.class), any(Long.class));
    }

    @Test
    @DisplayName("Получение запросов пользователя с валидным заголовком")
    void testGetItemRequestsWithValidHeader() throws Exception {
        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk());

        verify(itemRequestClient).getItemRequests(1L);
    }

    @Test
    @DisplayName("Получение всех запросов с валидным заголовком")
    void testGetAllItemRequestsWithValidHeader() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk());

        verify(itemRequestClient).getAllItemRequests(1L);
    }

    @Test
    @DisplayName("Получение конкретного запроса с валидным заголовком")
    void testGetItemRequestWithValidHeader() throws Exception {
        mockMvc.perform(get("/requests/123")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk());

        verify(itemRequestClient).getItemRequest(1L, 123L);
    }

    @Test
    @DisplayName("Создание запроса с дополнительными полями в DTO")
    void testPostItemRequestWithAllFields() throws Exception {
        ItemRequestDto validDto = ItemRequestDto.builder()
                .id(999L)
                .description("Нужен перфоратор")
                .requestorId(100L)
                .build();

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isOk());

        verify(itemRequestClient).postItemRequest(eq(1L), any(ItemRequestDto.class));
    }
}