package ru.practicum.shareit.user;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@ExtendWith(SpringExtension.class)
@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRepositoryTest {

    final UserRepository userRepository;
    final TestEntityManager manager;
    User request;

    @BeforeEach
    void setUp() {
        request = User.builder()
                .name("name")
                .email("mail@mail.ru")
                .build();
    }

    @DisplayName("Сохранение и получение пользователя по ID")
    @Test
    void saveAndGetUser_success() {
        User user = manager.persistFlushFind(request);

        assertThat(user.getId(), notNullValue());
        assertThat(user.getName(), equalTo(request.getName()));
        assertThat(user.getEmail(), equalTo(request.getEmail()));
    }

    @DisplayName("Обновление имени пользователя")
    @Test
    void updateUser_shouldUpdateName() {
        long id = (long) manager.persistAndGetId(request);
        User update = User.builder()
                .id(id)
                .name("newName")
                .email(request.getEmail())
                .build();

        User user = userRepository.save(update);
        manager.flush();

        assertThat(user.getName(), equalTo("newName"));

        List<User> users = userRepository.findAll();
        assertThat(users.size(), equalTo(1));
    }

    @DisplayName("Удаление пользователя")
    @Test
    void deleteUser_shouldRemoveFromDatabase() {
        User user = manager.persistFlushFind(request);
        Long userId = user.getId();
        assertThat(userId, notNullValue());

        userRepository.delete(user);
        manager.flush();

        Optional<User> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser.isEmpty(), is(true));
    }

    @DisplayName("Получение списка всех пользователей")
    @Test
    void getAllUsers_shouldReturnAllUsers() {
        User request2 = User.builder().name("name2").email("mail2@mail.ru").build();
        manager.persist(request2);
        manager.persist(request);

        List<User> users = userRepository.findAll();
        assertThat(users.size(), equalTo(2));
    }

    @DisplayName("Поиск пользователя по email")
    @Test
    void findByEmail_shouldReturnUser() {
        manager.persistAndFlush(request);

        Optional<User> found = userRepository.findByEmail(request.getEmail());

        assertThat(found.isPresent(), equalTo(true));
        assertThat(found.get().getName(), equalTo(request.getName()));
        assertThat(found.get().getEmail(), equalTo(request.getEmail()));
    }

}
