package ru.practicum.shareit.item;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByOwnerId(Long userId);

    Item save(Item item);

    @Modifying
    @Transactional
    @Query("DELETE FROM Item i WHERE i.id = :itemId AND i.ownerId = :userId")
    void deleteByUserIdAndItemId(long userId, long itemId);

    Optional<Item> findById(Long aLong);

    @Query("SELECT i FROM Item i " +
            "WHERE (LOWER(i.name) LIKE %:text% OR LOWER(i.description) LIKE %:text%) " +
            "AND i.available = true")
    List<Item> search(String text);

    List<Item> findAll();

}