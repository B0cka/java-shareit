package ru.practicum.shareit.user;

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
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@WebMvcTest(controllers = UserController.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserControllerTest {

    @MockBean
    UserService userService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    User user;
    User user2;

    @BeforeEach
    void beforeEach() {
        user = User.builder()
                .id(1L)
                .name("name")
                .email("email@mail.ru")
                .build();

        user2 = User.builder()
                .id(2L)
                .name("name2")
                .email("email2@mail.ru")
                .build();
    }

    @DisplayName("Получение всех пользователей")
    @Test
    void getAllUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(user, user2));

        mockMvc.perform(get("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("name")))
                .andExpect(jsonPath("$[0].email", is("email@mail.ru")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("name2")))
                .andExpect(jsonPath("$[1].email", is("email2@mail.ru")));
    }

    @DisplayName("Получение пользователя по ID")
    @Test
    void getUserById() throws Exception {
        when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/users/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("name")))
                .andExpect(jsonPath("$.email", is("email@mail.ru")));
    }

    @DisplayName("Создание пользователя")
    @Test
    void saveUser() throws Exception {
        when(userService.saveUser(any(User.class))).thenReturn(user);

        UserDto request = UserDto.builder().name("name").email("email@mail.ru").build();

        mockMvc.perform(post("/users")
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())  // если @ResponseStatus(HttpStatus.CREATED) не стоит
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("name")))
                .andExpect(jsonPath("$.email", is("email@mail.ru")));
    }

    @DisplayName("Обновление пользователя")
    @Test
    void updateUser() throws Exception {
        User updated = User.builder()
                .id(1L)
                .name("newName")
                .email("new@mail.ru")
                .build();

        when(userService.updateUser(any(Long.class), any(UserDto.class))).thenReturn(updated);

        UserDto updateRequest = UserDto.builder()
                .id(1L)
                .name("newName")
                .email("new@mail.ru")
                .build();

        mockMvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(updateRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("newName")))
                .andExpect(jsonPath("$.email", is("new@mail.ru")));
    }

    @DisplayName("Удаление пользователя")
    @Test
    void deleteUser() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
    }
}
