package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoBooking;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.impl.ItemServiceImpl;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemServiceImplTest {

    @InjectMocks
    ItemServiceImpl itemService;

    @Mock
    ItemRepository itemRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    CommentRepository commentRepository;
    @Mock
    ItemRequestRepository itemRequestRepository;

    final long userId = 1L;
    final long itemId = 1L;
    final long bookerId = 2L;

    User user;
    Item item;
    ItemDto itemDto;

    @BeforeEach
    void setup() {
        user = User.builder().id(userId).name("User").email("user@mail.com").build();
        item = Item.builder().id(itemId).name("Item").description("Description").available(true).ownerId(userId).build();
        itemDto = ItemDto.builder().name("Item").description("Description").available(true).build();
    }

    @Test
    @DisplayName("Успешное добавление новой вещи")
    void testAddNewItem() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.save(any(Item.class))).thenAnswer(i -> i.getArgument(0));

        ItemDto result = itemService.addNewItem(userId, itemDto);

        assertEquals(itemDto.getName(), result.getName());
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    @DisplayName("Обновление вещи владельцем")
    void testUpdateItem() {
        Item existingItem = Item.builder()
                .id(itemId).name("Old name").description("Old desc").available(true).ownerId(userId).build();

        ItemDto updateDto = ItemDto.builder().name("Updated name").description("Updated desc").available(false).build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemDto result = itemService.updateItem(userId, itemId, updateDto);

        assertEquals("Updated name", result.getName());
        assertEquals("Updated desc", result.getDescription());
        assertFalse(result.getAvailable());
    }

    @Test
    @DisplayName("Обновление вещи не владельцем")
    void testUpdateItem_NotOwner() {
        Item existingItem = Item.builder().id(itemId).ownerId(99L).build(); // другой владелец

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));

        ItemDto updateDto = ItemDto.builder().name("Updated").build();

        assertThrows(NotFoundException.class, () -> itemService.updateItem(userId, itemId, updateDto));
    }

    @Test
    @DisplayName("Поиск с пустым текстом")
    void testSearchItems_EmptyText() {
        List<ItemDto> result = itemService.searchItems("   ");
        assertTrue(result.isEmpty());
        verify(itemRepository, never()).search(any());
    }

    @Test
    @DisplayName("Поиск с текстом")
    void testSearchItems() {
        when(itemRepository.search("test")).thenReturn(List.of(item));
        List<ItemDto> result = itemService.searchItems("test");
        assertEquals(1, result.size());
        assertEquals(item.getName(), result.get(0).getName());
    }

    @Test
    @DisplayName("Получение вещи с комментариями")
    void testGetItemWithComments() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findByItem_idAndStatus(itemId, BookingStatus.WAITING)).thenReturn(List.of());
        when(commentRepository.findByItem_id(itemId)).thenReturn(List.of());

        ItemDtoBooking result = itemService.getItemWithComments(itemId);
        assertEquals(item.getId(), result.getId());
    }

    @Test
    @DisplayName("Добавление комментария — успешное")
    void testAddComment_Success() {
        User booker = User.builder().id(bookerId).name("Booker").email("booker@mail.com").build();
        Booking booking = Booking.builder()
                .id(10L)
                .booker(booker)
                .item(item)
                .endTime(LocalDateTime.now().minusDays(1))
                .status(BookingStatus.APPROVED)
                .build();

        CommentDto requestComment = CommentDto.builder().text("Test comment").build();

        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findFirstByBookerIdAndItemIdAndEndTimeBeforeOrderByEndTimeDesc(eq(bookerId), eq(itemId), any()))
                .thenReturn(booking);
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        CommentDto result = itemService.addComment(bookerId, itemId, requestComment);
        assertEquals("Test comment", result.getText());
        assertNotNull(result.getId());
    }

    @Test
    @DisplayName("Добавление комментария — без права доступа")
    void testAddComment_ValidationException() {
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findFirstByBookerIdAndItemIdAndEndTimeBeforeOrderByEndTimeDesc(eq(bookerId), eq(itemId), any()))
                .thenReturn(null);

        CommentDto commentDto = CommentDto.builder().text("Test").build();
        assertThrows(ValidationException.class, () -> itemService.addComment(bookerId, itemId, commentDto));
    }

    @Test
    @DisplayName("Удаление вещи пользователем")
    void testDeleteItem() {
        itemService.deleteItem(userId, itemId);
        verify(itemRepository).deleteByUserIdAndItemId(userId, itemId);
    }

    @Test
    @DisplayName("Получение комментариев пользователя")
    void testGetComments() {
        Comment comment = Comment.builder()
                .id(1L)
                .description("Comment")
                .author(user)
                .item(item)
                .created(LocalDateTime.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(commentRepository.findByAuthorId(userId)).thenReturn(List.of(comment));

        List<Comment> comments = itemService.getComments(userId);

        assertEquals(1, comments.size());
        assertEquals("Comment", comments.get(0).getDescription());
    }


    @Test
    @DisplayName("Получение комментариев — пользователь не найден")
    void testGetComments_UserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getComments(userId));
    }

    @Test
    @DisplayName("Получение всех вещей пользователя")
    void testAllItemsFromUser() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findByOwnerId(userId)).thenReturn(List.of(item));

        List<ItemDto> items = itemService.allItemsFormUser(userId);

        assertEquals(1, items.size());
        assertEquals(item.getName(), items.get(0).getName());
    }

    @Test
    @DisplayName("Получение всех вещей — пользователь не найден")
    void testAllItemsFromUser_UserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.allItemsFormUser(userId));
    }

    @Test
    @DisplayName("Ошибка при null available")
    void testAddNewItem_NullAvailable() {
        itemDto.setAvailable(null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        assertThrows(ValidationException.class, () -> itemService.addNewItem(userId, itemDto));
    }

    @Test
    @DisplayName("Ошибка при пустом имени")
    void testAddNewItem_EmptyName() {
        itemDto.setName("");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        assertThrows(ValidationException.class, () -> itemService.addNewItem(userId, itemDto));
    }

    @Test
    @DisplayName("Ошибка при null описании")
    void testAddNewItem_NullDescription() {
        itemDto.setDescription(null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        assertThrows(ValidationException.class, () -> itemService.addNewItem(userId, itemDto));
    }

    @Test
    @DisplayName("Ошибка при несуществующем пользователе")
    void testAddNewItem_UserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> itemService.addNewItem(userId, itemDto));
    }

    @Test
    @DisplayName("addNewItem — с ItemRequest")
    void testAddNewItem_WithRequest() {
        itemDto.setRequestId(1L);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(ItemRequest.builder().id(1L).build()));
        when(itemRepository.save(any(Item.class))).thenAnswer(i -> i.getArgument(0));

        ItemDto result = itemService.addNewItem(userId, itemDto);
        assertEquals(itemDto.getName(), result.getName());
    }
}
