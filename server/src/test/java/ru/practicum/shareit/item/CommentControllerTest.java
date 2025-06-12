package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.controller.CommentController;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoBooking;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CommentController.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentControllerTest {

    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemService itemService;

    @Autowired
    MockMvc mockMvc;

    UserDto owner;
    ItemDtoBooking itemDtoBooking;
    ItemDto itemDto;
    ItemDto itemDto2;
    List<ItemDto> items;
    CommentDto comment;

    @BeforeEach
    void setUp() {
        owner = UserDto.builder()
                .id(1L)
                .name("owner")
                .email("owner@mail.ru")
                .build();

        itemDtoBooking = ItemDtoBooking.builder()
                .id(3L)
                .name("дрель")
                .description("дрель ударная")
                .available(true)
                .build();

        itemDto = ItemDto.builder()
                .id(1L)
                .name("стол")
                .description("стол походный")
                .available(true)
                .build();

        itemDto2 = ItemDto.builder()
                .id(2L)
                .name("молоток")
                .description("универсальный инструмент на все случаи жизни")
                .available(true)
                .build();

        comment = CommentDto.builder()
                .id(1L)
                .text("супер")
                .authorName(owner.getName())
                .created(LocalDateTime.of(2025, 6, 11, 12, 30))
                .build();
    }

    @DisplayName("Добавление комментария")
    @Test
    void addCommentForItemTest() throws Exception {
        when(itemService.addComment(any(Long.class), any(Long.class), any(CommentDto.class))).thenReturn(comment);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(comment))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(comment.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(comment.getText())))
                .andExpect(jsonPath("$.authorName", is(comment.getAuthorName())));
    }

}
