package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoBooking;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

public interface ItemService {
    ItemDto addNewItem(Long userId, ItemDto itemDto);

    ItemDtoBooking getItemWithComments(Long itemId);

    void deleteItem(Long userId, Long itemId);

    ItemDto updateItem(long userId, long itemId, ItemDto itemDto);

    List<ItemDto> searchItems(String text);

    CommentDto addComment(long userId, long itemId, CommentDto commentDto);

    List<Comment> getComments(long userId);

    List<ItemDto> allItemsFormUser(long userId);
}
