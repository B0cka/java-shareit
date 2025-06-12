package ru.practicum.shareit.request;

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
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Transactional
@ExtendWith(SpringExtension.class)
@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRequestRepositoryTest {
    final ItemRequestRepository itemRequestRepository;
    final ItemRepository itemRepository;
    final TestEntityManager manager;
    ItemRequest itemRequest;
    User userRequestor;
    User userOwner;
    Item item;

    @BeforeEach
    void beforeEach() {
        userRequestor = manager.persistFlushFind(User.builder()
                .name("name")
                .email("mail@mail.ru")
                .build());

        itemRequest = manager.persistFlushFind(ItemRequest.builder()
                .description("дрель")
                .created(LocalDateTime.now())
                .requestor(userRequestor)
                .build());

        userOwner = manager.persistFlushFind(User.builder()
                .name("name2")
                .email("mail2@mail.ru")
                .build());

        item = manager.persistFlushFind(Item.builder()
                .name("дрель")
                .description("дрель ударная")
                .available(true)
                .request(itemRequest)
                .build());
    }

    @DisplayName("Получение всех ответов на запрос пользователя")
    @Test
    void getAnswersForRequest() {
        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userRequestor.getId());

        assertThat(requests.getFirst(), equalTo(itemRequest));

        List<Item> answers = itemRepository.findByRequestId(requests.getFirst().getId());

        assertThat(answers.getFirst(), equalTo(item));
    }

}
