package ru.practicum.shareit.item;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@ExtendWith(SpringExtension.class)
@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRepositoryTest {
    final ItemRepository itemRepository;
    final TestEntityManager manager;
    User user;
    Item item;
    Item item2;
    User user2;
    Item item3;
    Comment comment;

    @BeforeEach
    void beforeEach() {
        user = manager.persistFlushFind(User.builder()
                .name("name")
                .email("mail@mail.ru")
                .build());

        item = manager.persistFlushFind(Item.builder()
                .name("дрель")
                .description("дрель ударная")
                .available(true)
                .ownerId(user.getId())
                .build());

        item2 = manager.persistFlushFind(Item.builder()
                .name("Фотоаппарат")
                .description("Фотоаппарат зеркальный")
                .ownerId(user.getId())
                .available(true)
                .build());

        user2 = manager.persistFlushFind(User.builder()
                .name("name2")
                .email("mail2@mail.ru")
                .build());

        item3 = manager.persistFlushFind(Item.builder()
                .name("Стол")
                .description("Стол походный")
                .ownerId(user2.getId())
                .available(true)
                .build());

        comment = Comment.builder()
                .description("Гуд")
                .item(item2)
                .author(user)
                .created(LocalDateTime.now())
                .build();

    }

    @DisplayName("Сохранение и получение Item пользователя")
    @Test
    void saveAndGetItemsByOwner() {
        List<Item> itemsByOwner = itemRepository.findByOwnerId(user.getId());

        assertThat(itemsByOwner.size(), equalTo(2));
        assertThat(itemsByOwner.getFirst(), equalTo(item));
        assertThat(itemsByOwner.get(1), equalTo(item2));
    }

    @DisplayName("Поиск вещи по имени и описанию")
    @Test
    void searchItemByText() {
        List<Item> searchItem = itemRepository.search("стол");

        assertThat(searchItem.size(), equalTo(1));
        assertThat(searchItem.getFirst(), equalTo(item3));
    }

    @Test
    @DisplayName("Добавление комментария")
    void addComment() {
        User author = manager.persist(User.builder()
                .name("Test User")
                .email("test@example.com")
                .build());

        User owner = manager.persist(User.builder()
                .name("Owner")
                .email("owner@example.com")
                .build());

        Item item = manager.persist(Item.builder()
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .ownerId(owner.getId())
                .build());

        Booking booking = manager.persist(Booking.builder()
                .start(LocalDateTime.now().minusDays(2))
                .endTime(LocalDateTime.now().minusDays(1))
                .item(item)
                .booker(author)
                .status(BookingStatus.APPROVED)
                .build());

        Comment comment = Comment.builder()
                .item(item)
                .author(author)
                .description("Отличный товар!")
                .created(LocalDateTime.now())
                .booking(booking)
                .build();

        Comment saved = manager.persistFlushFind(comment);

        assertThat(saved.getId(), notNullValue());
        assertThat(saved.getItem(), equalTo(comment.getItem()));
        assertThat(saved.getDescription(), equalTo(comment.getDescription()));
        assertThat(saved.getAuthor(), equalTo(comment.getAuthor()));
        assertThat(saved.getCreated().truncatedTo(ChronoUnit.SECONDS),
                equalTo(comment.getCreated().truncatedTo(ChronoUnit.SECONDS)));
    }

}
