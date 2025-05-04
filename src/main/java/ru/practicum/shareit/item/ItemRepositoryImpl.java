package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ItemRepositoryImpl implements ItemRepository {

    private final Map<Long, Item> storage = new HashMap<>();
    private long currentId = 1;

    @Override
    public List<Item> findByUserId(long userId) {
        return storage.values().stream()
                .filter(item -> Objects.equals(item.getOwnerId(), userId))
                .collect(Collectors.toList());
    }

    public List<Item> findAll() {
        return new ArrayList<>(storage.values());
    }

    public List<Item> search(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        String lowerCaseText = text.toLowerCase();
        return storage.values().stream()
                .filter(item -> item.getAvailable() &&
                        (item.getName().toLowerCase(Locale.ROOT).contains(lowerCaseText) ||
                                item.getDescription().toLowerCase().contains(lowerCaseText)))
                .collect(Collectors.toList());
    }

    @Override
    public Item findById(long id) {
        return storage.get(id);
    }

    @Override
    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(currentId++);
        }
        storage.put(item.getId(), item);
        return item;
    }

    @Override
    public void deleteByUserIdAndItemId(long userId, long itemId) {
        Item existing = storage.get(itemId);
        if (existing != null && existing.getId() == userId) {
            storage.remove(itemId);
        }
    }
}
