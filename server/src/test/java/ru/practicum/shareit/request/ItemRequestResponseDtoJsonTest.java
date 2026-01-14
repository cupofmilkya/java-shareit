package ru.practicum.shareit.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestResponseDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestResponseDto> json;

    @Test
    void testSerialize() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .requestId(1L)
                .build();

        ItemRequestResponseDto dto = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Нужна дрель для ремонта")
                .requestorId(2L)
                .created(LocalDateTime.of(2024, 1, 1, 12, 0, 0))
                .items(List.of(itemDto))
                .build();

        JsonContent<ItemRequestResponseDto> result = json.write(dto);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).hasJsonPath("$.description");
        assertThat(result).hasJsonPath("$.requestorId");
        assertThat(result).hasJsonPath("$.created");
        assertThat(result).hasJsonPath("$.items");
        assertThat(result).hasJsonPath("$.items[0].id");
        assertThat(result).hasJsonPath("$.items[0].name");
        assertThat(result).hasJsonPath("$.items[0].description");
        assertThat(result).hasJsonPath("$.items[0].available");
        assertThat(result).hasJsonPath("$.items[0].requestId");

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Нужна дрель для ремонта");
        assertThat(result).extractingJsonPathNumberValue("$.requestorId").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2024-01-01T12:00:00");
        assertThat(result).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo("Дрель");
        assertThat(result).extractingJsonPathStringValue("$.items[0].description").isEqualTo("Аккумуляторная дрель");
        assertThat(result).extractingJsonPathBooleanValue("$.items[0].available").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.items[0].requestId").isEqualTo(1);
    }

    @Test
    void testSerializeWithoutItems() throws Exception {
        ItemRequestResponseDto dto = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Нужна дрель для ремонта")
                .requestorId(2L)
                .created(LocalDateTime.of(2024, 1, 1, 12, 0, 0))
                .items(null)  // без items
                .build();

        JsonContent<ItemRequestResponseDto> result = json.write(dto);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).hasJsonPath("$.description");
        assertThat(result).hasJsonPath("$.requestorId");
        assertThat(result).hasJsonPath("$.created");
        assertThat(result).hasJsonPath("$.items");

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Нужна дрель для ремонта");
        assertThat(result).extractingJsonPathNumberValue("$.requestorId").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2024-01-01T12:00:00");
        assertThat(result).extractingJsonPathValue("$.items").isNull();
    }

    @Test
    void testDeserialize() throws Exception {
        String jsonContent = "{" +
                "\"id\": 1," +
                "\"description\": \"Нужна дрель для ремонта\"," +
                "\"requestorId\": 2," +
                "\"created\": \"2024-01-01T12:00:00\"," +
                "\"items\": [" +
                "   {" +
                "       \"id\": 1," +
                "       \"name\": \"Дрель\"," +
                "       \"description\": \"Аккумуляторная дрель\"," +
                "       \"available\": true," +
                "       \"requestId\": 1" +
                "   }" +
                "]" +
                "}";

        ItemRequestResponseDto dto = json.parseObject(jsonContent);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getDescription()).isEqualTo("Нужна дрель для ремонта");
        assertThat(dto.getRequestorId()).isEqualTo(2L);
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2024, 1, 1, 12, 0, 0));
        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getItems().getFirst().getId()).isEqualTo(1L);
        assertThat(dto.getItems().getFirst().getName()).isEqualTo("Дрель");
        assertThat(dto.getItems().getFirst().getDescription()).isEqualTo("Аккумуляторная дрель");
        assertThat(dto.getItems().getFirst().getAvailable()).isTrue();
        assertThat(dto.getItems().getFirst().getRequestId()).isEqualTo(1L);
    }

    @Test
    void testDeserializeWithoutItems() throws Exception {
        String jsonContent = "{" +
                "\"id\": 1," +
                "\"description\": \"Нужна дрель для ремонта\"," +
                "\"requestorId\": 2," +
                "\"created\": \"2024-01-01T12:00:00\"" +
                "}";

        ItemRequestResponseDto dto = json.parseObject(jsonContent);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getDescription()).isEqualTo("Нужна дрель для ремонта");
        assertThat(dto.getRequestorId()).isEqualTo(2L);
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2024, 1, 1, 12, 0, 0));
        assertThat(dto.getItems()).isNull();
    }

    @Test
    void testDeserializeWithEmptyItemsArray() throws Exception {
        String jsonContent = "{" +
                "\"id\": 1," +
                "\"description\": \"Нужна дрель для ремонта\"," +
                "\"requestorId\": 2," +
                "\"created\": \"2024-01-01T12:00:00\"," +
                "\"items\": []" +
                "}";

        ItemRequestResponseDto dto = json.parseObject(jsonContent);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getDescription()).isEqualTo("Нужна дрель для ремонта");
        assertThat(dto.getRequestorId()).isEqualTo(2L);
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2024, 1, 1, 12, 0, 0));
        assertThat(dto.getItems()).isEmpty();
    }

    @Test
    void testJsonFormat() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        LocalDateTime created = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

        ItemRequestResponseDto dto = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Нужна дрель")
                .requestorId(2L)
                .created(created)
                .items(null)
                .build();

        String jsonString = mapper.writeValueAsString(dto);

        assertThat(jsonString)
                .contains("\"id\":1")
                .contains("\"description\":\"Нужна дрель\"")
                .contains("\"requestorId\":2")
                .contains("\"items\":null");

        assertThat(jsonString).contains("\"created\":");
    }
}
