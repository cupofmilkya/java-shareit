package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    private ItemRequestDto testRequestDto;
    private ItemRequestResponseDto testResponseDto;

    @BeforeEach
    void setUp() {
        testRequestDto = ItemRequestDto.builder()
                .description("Need a drill for home repairs")
                .build();

        testResponseDto = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Need a drill for home repairs")
                .requestorId(1L)
                .created(LocalDateTime.now())
                .items(List.of())
                .build();
    }

    @Test
    @DisplayName("Создание запроса на предмет")
    void testPostItemRequest() throws Exception {
        when(itemRequestService.postItemRequest(any(ItemRequestDto.class), eq(1L)))
                .thenReturn(testResponseDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Need a drill for home repairs"))
                .andExpect(jsonPath("$.requestorId").value(1L));

        verify(itemRequestService, times(1)).postItemRequest(any(ItemRequestDto.class), eq(1L));
    }

    @Test
    @DisplayName("Создание запроса без заголовка пользователя")
    void testPostItemRequestWithoutUserId() throws Exception {
        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequestDto)))
                .andExpect(status().isBadRequest());

        verify(itemRequestService, never()).postItemRequest(any(ItemRequestDto.class), anyLong());
    }

    @Test
    @DisplayName("Получение запросов пользователя")
    void testGetItemRequests() throws Exception {
        when(itemRequestService.getItemRequests(1L)).thenReturn(List.of(testResponseDto));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(itemRequestService, times(1)).getItemRequests(1L);
    }

    @Test
    @DisplayName("Получение запросов пользователя без заголовка")
    void testGetItemRequestsWithoutUserId() throws Exception {
        mockMvc.perform(get("/requests"))
                .andExpect(status().isBadRequest());

        verify(itemRequestService, never()).getItemRequests(anyLong());
    }

    @Test
    @DisplayName("Получение всех запросов")
    void testGetAllItemRequests() throws Exception {
        when(itemRequestService.getAllItemRequests()).thenReturn(List.of(testResponseDto));

        mockMvc.perform(get("/requests/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(itemRequestService, times(1)).getAllItemRequests();
    }

    @Test
    @DisplayName("Получение запроса по ID")
    void testGetItemRequestById() throws Exception {
        when(itemRequestService.getItemRequest(1L)).thenReturn(testResponseDto);

        mockMvc.perform(get("/requests/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Need a drill for home repairs"));

        verify(itemRequestService, times(1)).getItemRequest(1L);
    }

    @Test
    @DisplayName("Получение несуществующего запроса")
    void testGetNonExistentItemRequest() throws Exception {
        when(itemRequestService.getItemRequest(999L))
                .thenThrow(new ru.practicum.shareit.handler.exception.NotFoundException("Запрос не найден"));

        mockMvc.perform(get("/requests/999"))
                .andExpect(status().isNotFound());

        verify(itemRequestService, times(1)).getItemRequest(999L);
    }

    @Test
    @DisplayName("Получение запроса с предметами")
    void testGetItemRequestWithItems() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Power Drill")
                .description("Cordless drill")
                .available(true)
                .build();

        ItemRequestResponseDto responseWithItems = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Need a drill for home repairs")
                .requestorId(1L)
                .created(LocalDateTime.now())
                .items(List.of(itemDto))
                .build();

        when(itemRequestService.getItemRequest(1L)).thenReturn(responseWithItems);

        mockMvc.perform(get("/requests/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].name").value("Power Drill"));

        verify(itemRequestService, times(1)).getItemRequest(1L);
    }
}