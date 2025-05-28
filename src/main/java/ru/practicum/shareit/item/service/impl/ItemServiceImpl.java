package ru.practicum.shareit.item.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoBooking;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;


import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public ItemDto updateItem(long userId, long itemId, ItemDto itemDto) {
        log.info("Update item with id={} by userId={}", itemId, userId);

        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));
        if (existingItem == null) {
            throw new NotFoundException("Item not found");
        }

        if (!existingItem.getOwnerId().equals(userId)) {
            throw new NotFoundException("Only owner can edit item");
        }

        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        return ItemMapper.toItemDto(itemRepository.save(existingItem));
    }


    public List<ItemDto> searchItems(String text) {
        log.info("request to search {}", text);
        if (text.isBlank()) {
            log.info("text is blank");
            return Collections.emptyList();
        }
        log.info("all items: {}", itemRepository.findAll());
        List<Item> foundItems = itemRepository.search(text.toLowerCase());
        log.info("found items: {}", foundItems);
        return foundItems.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }


    @Override
    public ItemDto addNewItem(Long userId, ItemDto itemDto) {
        Item item = ItemMapper.toItem(itemDto, userId);
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("User with id " + userId + " not found");
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Available must be not empty!");
        }
        if (itemDto.getName() == "" || itemDto.getName() == null) {
            throw new ValidationException("Name must be not empty!");
        }
        if (itemDto.getDescription() == null) {
            throw new ValidationException("Description must be not empty!");
        }
        item.setOwnerId(userId);
        Item saved = itemRepository.save(item);
        return ItemMapper.toItemDto(saved);
    }

    @Override
    public ItemDtoBooking getItemWithComments(Long itemId) {
        log.info("request to get item with id {}", itemId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет не найден"));

        List<Booking> bookings = bookingRepository.findByItem_idAndStatus(itemId, BookingStatus.WAITING);

        List<Comment> comments = commentRepository.findByItem_id(itemId);

        ItemDtoBooking itemDtoBooking = ItemMapper.mapToItemDtoBooking(item,
                bookings,
                comments);

        return itemDtoBooking;
    }

    @Override
    public void deleteItem(Long userId, Long itemId) {
        log.info("Удаление предмета");
        itemRepository.deleteByUserIdAndItemId(userId, itemId);
    }

    @Override
    public CommentDto addComment(long userId, long itemId, CommentDto commentDto) {
        log.info("Добавление комментария от пользователя {} к вещи {}", userId, itemId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found with id: " + itemId));

        Booking booking = bookingRepository.findFirstByBookerIdAndItemIdAndEndTimeBeforeOrderByEndTimeDesc(
                userId, itemId, LocalDateTime.now()
        ).orElseThrow(() -> new ValidationException("User has not completed booking for this item"));

        Comment comment = new Comment();
        comment.setAuthor(user);
        comment.setItem(item);
        comment.setDescription(commentDto.getText());
        comment.setCreated(LocalDateTime.now());
        comment.setBooking(booking);

        Comment saved = commentRepository.save(comment);

        return ItemMapper.mapToDto(saved);
    }

    public List<Comment> getComments(long userId) {
        log.info("Запрос на получение всех комментариев user с id: " + userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        return commentRepository.findByAuthorId(userId);
    }

    public List<ItemDto> allItemsFormUser(long userId){
        log.info("reauest to get all items from user with id: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        List<Item> item = itemRepository.findByOwnerId(userId);

        return item.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList()
        );
    }

}
