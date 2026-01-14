package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingForItemDtoJsonTest {

    @Autowired
    private JacksonTester<BookingForItemDto> json;

    @Test
    void testSerialize() throws Exception {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 2, 12, 0, 0);

        BookingForItemDto dto = BookingForItemDto.builder()
                .id(1L)
                .start(start)
                .end(end)
                .bookerId(2L)
                .status(BookingStatus.WAITING)
                .build();

        JsonContent<BookingForItemDto> result = json.write(dto);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).hasJsonPath("$.start");
        assertThat(result).hasJsonPath("$.end");
        assertThat(result).hasJsonPath("$.bookerId");
        assertThat(result).hasJsonPath("$.status");

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isNotNull();
        assertThat(result).extractingJsonPathStringValue("$.end").isNotNull();
        assertThat(result).extractingJsonPathNumberValue("$.bookerId").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
    }

    @Test
    void testSerializeWithDifferentStatus() throws Exception {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 2, 12, 0, 0);

        BookingForItemDto dto = BookingForItemDto.builder()
                .id(1L)
                .start(start)
                .end(end)
                .bookerId(2L)
                .status(BookingStatus.APPROVED)
                .build();

        JsonContent<BookingForItemDto> result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("APPROVED");
    }

    @Test
    void testDeserialize() throws Exception {
        String jsonContent = "{" +
                "\"id\": 1," +
                "\"start\": [2024,1,1,10,0]," +
                "\"end\": [2024,1,2,12,0]," +
                "\"bookerId\": 2," +
                "\"status\": \"WAITING\"" +
                "}";

        BookingForItemDto dto = json.parseObject(jsonContent);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2024, 1, 2, 12, 0, 0));
        assertThat(dto.getBookerId()).isEqualTo(2L);
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void testDeserializeAllStatuses() throws Exception {
        String[] statuses = {"WAITING", "APPROVED", "REJECTED", "CANCELED"};

        for (String status : statuses) {
            String jsonContent = "{" +
                    "\"id\": 1," +
                    "\"start\": [2024,1,1,10,0]," +
                    "\"end\": [2024,1,2,12,0]," +
                    "\"bookerId\": 2," +
                    "\"status\": \"" + status + "\"" +
                    "}";

            BookingForItemDto dto = json.parseObject(jsonContent);
            assertThat(dto.getStatus().toString()).isEqualTo(status);
        }
    }
}