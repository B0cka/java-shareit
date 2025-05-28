package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerIdOrderByStartDesc(Long userId);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId);

    Optional<Booking> findFirstByBookerIdAndItemIdAndEndTimeBeforeOrderByEndTimeDesc(
            Long bookerId, Long itemId, LocalDateTime now);

    List<Booking> findByItemidAndStatus(long itemId, BookingStatus status);

}
