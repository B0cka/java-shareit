package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;


import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto updateItem(long userId, long itemId, ItemDto itemDto) {
        log.info("Update item with id={} by userId={}", itemId, userId);

        Item existingItem = itemRepository.findById(itemId);
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
    public ItemDto findItem(long itemId){
        log.info("find item by id: {}", itemId);
        Item item = itemRepository.findById(itemId);
        if (item == null) {
            throw new NotFoundException("Item with id " + itemId + " not found");
        }
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto addNewItem(Long userId, ItemDto itemDto) {
        Item item = ItemMapper.toItem(itemDto, userId);
        if(userRepository.findById(userId).isEmpty()){
            throw new NotFoundException("User with id " + userId + " not found");
        }
        if(itemDto.getAvailable() == null){
            throw new ValidationException("Available must be not empty!");
        }
        if(itemDto.getName() == "" || itemDto.getName() == null){
            throw new ValidationException("Name must be not empty!");
        }
        if(itemDto.getDescription() == null){
            throw new ValidationException("Description must be not empty!");
        }
        item.setOwnerId(userId);
        Item saved = itemRepository.save(item);
        return ItemMapper.toItemDto(saved);
    }

    @Override
    public List<ItemDto> getItems(Long userId) {
        log.info("Запрос на все предметы");
        return itemRepository.findByUserId(userId)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteItem(Long userId, Long itemId) {
        log.info("Удаление предмета");
        itemRepository.deleteByUserIdAndItemId(userId, itemId);
    }

}
