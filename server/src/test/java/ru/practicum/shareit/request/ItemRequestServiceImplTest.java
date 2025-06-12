package ru.practicum.shareit.request;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.impl.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemRequestServiceImplTest {

    @InjectMocks
    ItemRequestServiceImpl itemRequestService;

    @Mock
    UserRepository userRepository;

    @Mock
    ItemRequestRepository itemRequestRepository;

    @Mock
    ItemRepository itemRepository;

    final long userId = 1L;
    final long requestId = 1L;

    final User user = User.builder()
            .id(userId)
            .name("User")
            .email("user@email.com")
            .build();

    final ItemRequestCreateDto requestDto = ItemRequestCreateDto.builder()
            .description("Need item")
            .build();

    final ItemRequest itemRequest = ItemRequest.builder()
            .id(requestId)
            .description("Need item")
            .requestor(user)
            .created(LocalDateTime.now())
            .build();

    @Test
    @DisplayName("Успешное создание запроса")
    void createRequestSuccess() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);

        ItemRequestDto result = itemRequestService.createRequest(userId, requestDto);

        assertNotNull(result);
        assertEquals("Need item", result.getDescription());
        assertNotNull(result.getCreated());
        assertTrue(result.getItems().isEmpty());

        verify(userRepository).findById(userId);
        verify(itemRequestRepository).save(any(ItemRequest.class));
    }

    @Test
    @DisplayName("Создание запроса с несуществующим пользователем")
    void createRequestUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.createRequest(userId, requestDto));
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Получение запросов пользователя")
    void getRequestsSuccess() {
        Item item = Item.builder()
                .id(10L)
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .request(itemRequest)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId)).thenReturn(List.of(itemRequest));
        when(itemRepository.findAllByRequestIdIn(List.of(requestId))).thenReturn(List.of(item));

        List<ItemRequestDto> result = itemRequestService.getRequests(userId);

        assertEquals(1, result.size());
        ItemRequestDto dto = result.get(0);
        assertEquals(requestId, dto.getId());
        assertEquals("Need item", dto.getDescription());
        assertEquals(1, dto.getItems().size());

        verify(itemRequestRepository).findByRequestorIdOrderByCreatedDesc(userId);
        verify(itemRepository).findAllByRequestIdIn(List.of(requestId));
    }

    @Test
    @DisplayName("Получение всех чужих запросов")
    void getAllRequestsSuccess() {
        ItemRequest otherRequest = ItemRequest.builder()
                .id(2L)
                .description("Another request")
                .requestor(User.builder().id(2L).name("Other").email("o@email.com").build())
                .created(LocalDateTime.now())
                .build();

        Item item = Item.builder()
                .id(11L)
                .name("Screwdriver")
                .description("Flathead")
                .available(true)
                .request(otherRequest)
                .build();

        when(itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId)).thenReturn(List.of(otherRequest));
        when(itemRepository.findAllByRequestIdIn(List.of(2L))).thenReturn(List.of(item));

        List<ItemRequestDto> result = itemRequestService.getAllRequests(userId);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
        assertEquals("Another request", result.get(0).getDescription());
        assertEquals(1, result.get(0).getItems().size());

        verify(itemRequestRepository).findAllByRequestorIdNotOrderByCreatedDesc(userId);
        verify(itemRepository).findAllByRequestIdIn(List.of(2L));
    }

    @Test
    @DisplayName("Получение запроса по id")
    void getRequestByIdSuccess() {
        Item item = Item.builder()
                .id(20L)
                .name("Hammer")
                .description("Heavy hammer")
                .available(true)
                .request(itemRequest)
                .build();

        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(itemRequest));
        when(itemRepository.findAllByRequestId(requestId)).thenReturn(List.of(item));

        ItemRequestDto result = itemRequestService.getRequestsById(requestId);

        assertNotNull(result);
        assertEquals(requestId, result.getId());
        assertEquals("Need item", result.getDescription());
        assertEquals(1, result.getItems().size());

        verify(itemRequestRepository).findById(requestId);
        verify(itemRepository).findAllByRequestId(requestId);
    }

    @Test
    @DisplayName("Получение запроса по несуществующему id")
    void getRequestByIdNotFound() {
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.getRequestsById(requestId));
        verify(itemRequestRepository).findById(requestId);
    }
}

