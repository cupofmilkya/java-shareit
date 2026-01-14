package ru.practicum.shareit.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserDtoJsonTest {

    @Autowired
    private JacksonTester<UserDto> json;

    @Test
    void testSerialize() throws Exception {
        UserDto dto = UserDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .build();

        JsonContent<UserDto> result = json.write(dto);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).hasJsonPath("$.name");
        assertThat(result).hasJsonPath("$.email");

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("John Doe");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("john.doe@example.com");
    }

    @Test
    void testSerializeWithMinimalData() throws Exception {
        UserDto dto = UserDto.builder()
                .id(0L)
                .name("Jane")
                .email("jane@example.com")
                .build();

        JsonContent<UserDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(0);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Jane");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("jane@example.com");
    }

    @Test
    void testSerializeWithSpecialCharacters() throws Exception {
        UserDto dto = UserDto.builder()
                .id(100L)
                .name("Иван Иванов")
                .email("иван@пример.рф")
                .build();

        JsonContent<UserDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(100);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Иван Иванов");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("иван@пример.рф");
    }

    @Test
    void testSerializeWithLongData() throws Exception {
        UserDto dto = UserDto.builder()
                .id(999999L)
                .name("Very Long Name With Spaces And Additional Information")
                .email("very.long.email.address.with.many.parts.and.subdomains@example.company.co.uk")
                .build();

        JsonContent<UserDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(999999);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Very Long Name With Spaces And Additional Information");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("very.long.email.address.with.many.parts.and.subdomains@example.company.co.uk");
    }

    @Test
    void testDeserialize() throws Exception {
        String jsonContent = "{" +
                "\"id\": 1," +
                "\"name\": \"John Doe\"," +
                "\"email\": \"john.doe@example.com\"" +
                "}";

        UserDto dto = json.parseObject(jsonContent);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("John Doe");
        assertThat(dto.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void testDeserializeWithNullFields() throws Exception {
        String jsonContent = "{" +
                "\"id\": 1" +
                "}";

        UserDto dto = json.parseObject(jsonContent);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isNull();
        assertThat(dto.getEmail()).isNull();
    }

    @Test
    void testDeserializeWithEmptyStrings() throws Exception {
        String jsonContent = "{" +
                "\"id\": 1," +
                "\"name\": \"\"," +
                "\"email\": \"\"" +
                "}";

        UserDto dto = json.parseObject(jsonContent);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEmpty();
        assertThat(dto.getEmail()).isEmpty();
    }

    @Test
    void testDeserializePartialUpdate() throws Exception {
        String jsonContent = "{" +
                "\"name\": \"Updated Name\"" +
                "}";

        UserDto dto = json.parseObject(jsonContent);

        assertThat(dto.getId()).isEqualTo(0L);
        assertThat(dto.getName()).isEqualTo("Updated Name");
        assertThat(dto.getEmail()).isNull();
    }

    @Test
    void testDeserializeOnlyEmailUpdate() throws Exception {
        String jsonContent = "{" +
                "\"email\": \"new.email@example.com\"" +
                "}";

        UserDto dto = json.parseObject(jsonContent);

        assertThat(dto.getId()).isEqualTo(0L);
        assertThat(dto.getName()).isNull();
        assertThat(dto.getEmail()).isEqualTo("new.email@example.com");
    }

    @Test
    void testJsonFormat() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        UserDto dto = UserDto.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();

        String jsonString = mapper.writeValueAsString(dto);

        assertThat(jsonString)
                .contains("\"id\":1")
                .contains("\"name\":\"Test User\"")
                .contains("\"email\":\"test@example.com\"");

        int idIndex = jsonString.indexOf("\"id\"");
        int nameIndex = jsonString.indexOf("\"name\"");
        int emailIndex = jsonString.indexOf("\"email\"");

        assertThat(idIndex).isLessThan(nameIndex);
        assertThat(nameIndex).isLessThan(emailIndex);
    }

    @Test
    void testCycleSerializationDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        UserDto original = UserDto.builder()
                .id(42L)
                .name("Original Name")
                .email("original@example.com")
                .build();

        String json = mapper.writeValueAsString(original);
        UserDto deserialized = mapper.readValue(json, UserDto.class);

        assertThat(deserialized.getId()).isEqualTo(original.getId());
        assertThat(deserialized.getName()).isEqualTo(original.getName());
        assertThat(deserialized.getEmail()).isEqualTo(original.getEmail());
        assertThat(deserialized).isNotSameAs(original);
    }

    @Test
    void testWithDifferentEmailFormats() throws Exception {
        String[] emails = {
                "simple@example.com",
                "user.name@example.com",
                "user_name@example.com",
                "user-name@example.com",
                "user+tag@example.com",
                "user@sub.example.com",
                "user@example.co.uk",
                "user@example.travel",
                "123456@example.com",
                "user@123.456.789.0",
                "user@[123.456.789.0]"
        };

        for (String email : emails) {
            UserDto dto = UserDto.builder()
                    .id(1L)
                    .name("Test")
                    .email(email)
                    .build();

            JsonContent<UserDto> result = json.write(dto);
            assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo(email);
        }
    }

    @Test
    void testEdgeCases() throws Exception {
        UserDto maxLongDto = UserDto.builder()
                .id(Long.MAX_VALUE)
                .name("Max")
                .email("max@example.com")
                .build();

        JsonContent<UserDto> maxResult = json.write(maxLongDto);
        assertThat(maxResult).extractingJsonPathNumberValue("$.id").isEqualTo(Long.MAX_VALUE);

        UserDto minLongDto = UserDto.builder()
                .id(Long.MIN_VALUE)
                .name("Min")
                .email("min@example.com")
                .build();

        JsonContent<UserDto> minResult = json.write(minLongDto);
        assertThat(minResult).extractingJsonPathNumberValue("$.id").isEqualTo(Long.MIN_VALUE);
    }

    @Test
    void testWithWhitespaceInName() throws Exception {
        UserDto dto = UserDto.builder()
                .id(1L)
                .name("  John  Doe  ")
                .email("john@example.com")
                .build();

        JsonContent<UserDto> result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("  John  Doe  ");
    }

    @Test
    void testCaseSensitivity() throws Exception {
        UserDto dto = UserDto.builder()
                .id(1L)
                .name("John Doe")
                .email("John.Doe@Example.COM")
                .build();

        JsonContent<UserDto> result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("John.Doe@Example.COM");
    }
}