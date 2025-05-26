package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

public interface ItemService {
    ItemDto addNewItem(Long userId, ItemDto itemDto);

    List<ItemDto> getItems(Long userId);

    void deleteItem(Long userId, Long itemId);

    ItemDto updateItem(long userId, long itemId, ItemDto itemDto);

    ItemDto findItem(long itemId);

    List<ItemDto> searchItems(String text);

    Comment addComment(long userId,long itemId, String description);

    List<Comment> getComments(long userId);

    List<Comment> getCommentsById(long userId, long itemId);
}
