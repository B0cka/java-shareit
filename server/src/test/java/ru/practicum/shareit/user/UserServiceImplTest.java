package ru.practicum.shareit.user;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.impl.UserServiceImpl;


import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class UserServiceImplTest {

    @InjectMocks
    UserServiceImpl userService;

    @Mock
    UserRepository userRepository;

    final Long userId = 1L;
    final User user = User.builder()
            .id(userId)
            .name("User 1")
            .email("user1@example.com")
            .build();

    @Test
    @DisplayName("Получение всех пользователей")
    void getAllUsers_success() {
        User user2 = User.builder()
                .id(2L)
                .name("User 2")
                .email("user2@example.com")
                .build();

        when(userRepository.findAll()).thenReturn(List.of(user, user2));

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("User 1", result.get(0).getName());
        assertEquals("User 2", result.get(1).getName());
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Получение пользователя по ID — успех")
    void getUserById_success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.getUserById(userId);

        assertEquals(user.getEmail(), result.getEmail());
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Получение пользователя по ID — не найден")
    void getUserById_notFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.getUserById(userId));
        assertTrue(ex.getMessage().contains("Пользователь не найден"));
    }

    @Test
    @DisplayName("Сохранение нового пользователя — успех")
    void saveUser_success() {
        when(userRepository.findAll()).thenReturn(List.of());
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.saveUser(user);

        assertEquals(user.getName(), result.getName());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Сохранение пользователя — дубликат email")
    void saveUser_duplicateEmail() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        User duplicate = User.builder()
                .name("Another")
                .email("user1@example.com")
                .build();

        assertThrows(ConflictException.class, () -> userService.saveUser(duplicate));
    }

    @Test
    @DisplayName("Сохранение пользователя — email null")
    void saveUser_invalidEmail() {
        User invalidUser = User.builder()
                .name("Name")
                .email(null)
                .build();

        assertThrows(ValidationException.class, () -> userService.saveUser(invalidUser));
    }

    @Test
    @DisplayName("Сохранение пользователя — email без @")
    void saveUser_invalidEmailFormat() {
        User invalidUser = User.builder()
                .name("Name")
                .email("invalid.email.com")
                .build();

        assertThrows(ValidationException.class, () -> userService.saveUser(invalidUser));
    }

    @Test
    @DisplayName("Обновление пользователя — успех")
    void updateUser_success() {
        UserDto updatedDto = UserDto.builder()
                .name("New Name")
                .email("new@mail.com")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("new@mail.com")).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(
                User.builder().id(userId).name("New Name").email("new@mail.com").build()
        );

        User result = userService.updateUser(userId, updatedDto);

        assertEquals("New Name", result.getName());
        assertEquals("new@mail.com", result.getEmail());
    }

    @Test
    @DisplayName("Обновление пользователя — не найден")
    void updateUser_notFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserDto dto = UserDto.builder().name("Any").email("any@mail.com").build();

        assertThrows(RuntimeException.class, () -> userService.updateUser(userId, dto));
    }

    @Test
    @DisplayName("Обновление пользователя — email уже занят")
    void updateUser_duplicateEmail() {
        UserDto dto = UserDto.builder()
                .name("New")
                .email("taken@mail.com")
                .build();

        User otherUser = User.builder()
                .id(2L)
                .name("Other")
                .email("taken@mail.com")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("taken@mail.com")).thenReturn(Optional.of(otherUser));

        assertThrows(ConflictException.class, () -> userService.updateUser(userId, dto));
    }


    @Test
    @DisplayName("Удаление пользователя — успех")
    void deleteUser_success() {
        doNothing().when(userRepository).deleteById(userId);

        userService.deleteUser(userId);

        verify(userRepository).deleteById(userId);
    }
}
