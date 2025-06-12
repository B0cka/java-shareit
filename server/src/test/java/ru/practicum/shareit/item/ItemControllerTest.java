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

import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemControllerTest {

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

    @DisplayName("Получение по id")
    @Test
    void getItemByIdTest() throws Exception {
        when(itemService.getItemWithComments(any(Long.class))).thenReturn(itemDtoBooking);

        mockMvc.perform(get("/items/1")
                        .content(mapper.writeValueAsString(itemDtoBooking))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDtoBooking.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDtoBooking.getName())))
                .andExpect(jsonPath("$.description", is(itemDtoBooking.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDtoBooking.getAvailable())));

    }

    @DisplayName("Получение всех вещей пользователя")
    @Test
    void getUserItemsTest() throws Exception {
        items = List.of(itemDto, itemDto2);
        when(itemService.allItemsFormUser(any(Long.class))).thenReturn(items);

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(items))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(itemDto2.getId()), Long.class));
    }

    @DisplayName("Поиск вещи по запросу")
    @Test
    void searchItemTest() throws Exception {
        items = List.of(itemDto);
        when(itemService.searchItems(any(String.class))).thenReturn(items);

        mockMvc.perform(get("/items/search")
                        .content(mapper.writeValueAsString(items))
                        .param("text", "стол")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class));
    }


    @DisplayName("Обновление вещи")
    @Test
    void updateItemTest() throws Exception {
        ItemDto updateItem = ItemDto.builder()
                .id(2L)
                .name("молоток новый")
                .description("универсальный инструмент на все случаи жизни, без криминала")
                .available(false)
                .build();
        when(itemService.updateItem(any(Long.class), any(Long.class), any(ItemDto.class))).thenReturn(updateItem);

        mockMvc.perform(patch("/items/2")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(updateItem))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updateItem.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(updateItem.getName())))
                .andExpect(jsonPath("$.description", is(updateItem.getDescription())))
                .andExpect(jsonPath("$.available", is(updateItem.getAvailable())));
    }


    @DisplayName("Получение несуществующей вещи")
    @Test
    void findNonExistentUser() throws Exception {
        long itemId = 999L;

        when(itemService.getItemWithComments(itemId))
                .thenThrow(new NotFoundException("Item not found"));

        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header("X-Sharer-Item-Id", itemId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Item not found")));
    }

    @DisplayName("Создание вещи")
    @Test
    void createItemTest() throws Exception {
        when(itemService.addNewItem(any(Long.class), any(ItemDto.class))).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));
    }
}
