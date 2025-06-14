package ru.practicum.shareit.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoBooking;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemMapperTest {

    @Test
    @DisplayName("Преобразование ItemDto в Item")
    void toItem_shouldMapDtoToItemCorrectly() {
        ItemDto dto = ItemDto.builder()
                .name("Отвертка")
                .description("Крестовая")
                .available(true)
                .build();

        Item item = ItemMapper.toItem(dto, 1L);

        assertEquals("Отвертка", item.getName());
        assertEquals("Крестовая", item.getDescription());
        assertTrue(item.getAvailable());
        assertEquals(1L, item.getOwnerId());
    }

    @Test
    @DisplayName("Преобразование Item в ItemDto")
    void toItemDto_shouldMapItemCorrectly() {
        Item item = Item.builder()
                .id(5L)
                .name("Молоток")
                .description("Стальной")
                .available(true)
                .build();

        ItemDto dto = ItemMapper.toItemDto(item);

        assertEquals(5L, dto.getId());
        assertEquals("Молоток", dto.getName());
        assertEquals("Стальной", dto.getDescription());
        assertTrue(dto.getAvailable());
    }

    @Test
    @DisplayName("Преобразование списка Item в список ItemDto")
    void toItemDtoList_shouldMapListCorrectly() {
        Item item1 = Item.builder().id(1L).name("1").description("d1").available(true).build();
        Item item2 = Item.builder().id(2L).name("2").description("d2").available(false).build();

        List<ItemDto> result = ItemMapper.toItemDtoList(List.of(item1, item2));

        assertEquals(2, result.size());
        assertEquals("1", result.get(0).getName());
        assertFalse(result.get(1).getAvailable());
    }

    @Test
    @DisplayName("Преобразование Comment в CommentDto")
    void mapToDto_shouldMapCommentCorrectly() {
        User author = User.builder().id(1L).name("Анна").build();
        Comment comment = Comment.builder()
                .id(10L)
                .description("Хорошая вещь")
                .author(author)
                .created(LocalDateTime.now())
                .build();

        CommentDto dto = ItemMapper.mapToDto(comment);

        assertEquals("Анна", dto.getAuthorName());
        assertEquals("Хорошая вещь", dto.getText());
        assertNotNull(dto.getCreated());
    }

    @Test
    @DisplayName("mapToItemDtoBooking: bookings пустой")
    void mapToItemDtoBooking_shouldHandleEmptyBookings() {
        Item item = Item.builder()
                .id(1L).name("Тест").description("desc").available(true)
                .build();

        ItemDtoBooking dto = ItemMapper.mapToItemDtoBooking(item, List.of(), List.of());

        assertNotNull(dto);
        assertNull(dto.getLastBooking());
        assertNull(dto.getNextBooking());
        assertTrue(dto.getComments().isEmpty());
    }

    @Test
    @DisplayName("toItem: корректная обработка null в полях ItemDto")
    void toItem_shouldHandleNullFieldsInDto() {
        ItemDto dto = ItemDto.builder()
                .name(null)
                .description(null)
                .available(false)
                .build();

        Item item = ItemMapper.toItem(dto, 1L);

        assertNull(item.getName());
        assertNull(item.getDescription());
        assertFalse(item.getAvailable());
        assertEquals(1L, item.getOwnerId());
    }

    @Test
    @DisplayName("mapToItemDtoBooking: комментарии корректно преобразуются")
    void mapToItemDtoBooking_shouldMapComments() {
        Item item = Item.builder()
                .id(1L).name("Тест").description("desc").available(true)
                .build();

        User user = User.builder().id(1L).name("Пользователь").build();
        Comment comment = Comment.builder()
                .id(100L)
                .description("Комментарий")
                .author(user)
                .created(LocalDateTime.now())
                .build();

        ItemDtoBooking dto = ItemMapper.mapToItemDtoBooking(item, List.of(), List.of(comment));

        assertEquals(1, dto.getComments().size());
        CommentDto commentDto = dto.getComments().get(0);
        assertEquals("Пользователь", commentDto.getAuthorName());
        assertEquals("Комментарий", commentDto.getText());
        assertNotNull(commentDto.getCreated());
    }

}
