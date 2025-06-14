package ru.practicum.shareit.item.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByOwnerId(Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Item i WHERE i.id = :itemId AND i.ownerId = :userId")
    void deleteByUserIdAndItemId(long userId, long itemId);

    @Query("SELECT i FROM Item i " +
            "WHERE (LOWER(i.name) LIKE %:text% OR LOWER(i.description) LIKE %:text%) " +
            "AND i.available = true")
    List<Item> search(String text);

    List<Item> findAllByRequestIdIn(List<Long> requestorsId);

    List<Item> findAllByRequestId(Long requestId);

    List<Item> findByRequestId(Long id);
}