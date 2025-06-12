package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@WebMvcTest(controllers = ItemRequestController.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRequestControllerTest {

    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemRequestService itemRequestService;

    @Autowired
    MockMvc mockMvc;

    UserDto user;
    ItemDto itemDto;
    ItemRequestDto itemRequestDto;
    ItemRequestCreateDto createDto;
    List<ItemRequestDto> requests;

    @BeforeEach
    void setUp() {
        user = UserDto.builder()
                .id(1L)
                .name("User Name")
                .email("user@example.com")
                .build();

        itemDto = ItemDto.builder()
                .id(1L)
                .name("Item Name")
                .description("Item Description")
                .available(true)
                .requestId(1L)
                .build();

        createDto = ItemRequestCreateDto.builder()
                .description("Need item")
                .build();

        itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Need item")
                .created(LocalDateTime.of(2025, 10, 3, 11, 30, 10))
                .items(List.of(itemDto))
                .build();

        requests = List.of(itemRequestDto);
    }

    @Test
    @DisplayName("Создание запроса вещи")
    void createRequestTest() throws Exception {
        when(itemRequestService.createRequest(1L, createDto)).thenReturn(itemRequestDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(createDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.created", is(itemRequestDto.getCreated().toString())));

    }

    @Test
    @DisplayName("Получение всех запросов пользователя")
    void getRequestsByUserIdTest() throws Exception {
        when(itemRequestService.getRequests(1L)).thenReturn(requests);

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$[0].created", is(itemRequestDto.getCreated().toString())));

    }

    @Test
    @DisplayName("Получение всех запросов от других пользователей")
    void getAllRequestsTest() throws Exception {
        when(itemRequestService.getAllRequests(1L)).thenReturn(requests);

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$[0].created", is(itemRequestDto.getCreated().toString())));

    }

    @Test
    @DisplayName("Получение запроса по id")
    void getRequestByIdTest() throws Exception {
        when(itemRequestService.getRequestsById(1L)).thenReturn(itemRequestDto);

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.created", is(itemRequestDto.getCreated().toString())));

    }

    @Test
    @DisplayName("Запрос по несуществующему ID вызывает 404")
    void getNonExistentRequestByIdTest() throws Exception {
        long requestId = 999L;
        when(itemRequestService.getRequestsById(requestId))
                .thenThrow(new NotFoundException("ItemRequest not found"));

        mockMvc.perform(get("/requests/{id}", requestId)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("ItemRequest not found")));
    }

    @Test
    @DisplayName("Запросы пользователя с несуществующим ID вызывают 404")
    void getRequestsByNonExistentUserIdTest() throws Exception {
        long userId = 999L;
        when(itemRequestService.getRequests(userId))
                .thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("User not found")));
    }
}
