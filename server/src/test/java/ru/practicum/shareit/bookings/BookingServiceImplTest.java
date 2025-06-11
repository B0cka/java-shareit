package ru.practicum.shareit.bookings;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.impl.BookingServiceImpl;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class BookingServiceImplTest {

    @InjectMocks
    BookingServiceImpl bookingService;

    @Mock
    BookingRepository bookingRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    ItemRepository itemRepository;

    final long userId = 1L;
    final long itemId = 1L;
    final long bookingId = 1L;

    final User user = User.builder().id(userId).name("User").email("user@mail.com").build();
    final User owner = User.builder().id(2L).name("Owner").email("owner@mail.com").build();
    final Item item = Item.builder().id(itemId).name("Item").available(true).ownerId(owner.getId()).build();
    final LocalDateTime start = LocalDateTime.now().plusDays(1);
    final LocalDateTime end = start.plusDays(1);

    final BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
            .itemId(itemId)
            .start(start)
            .end(end)
            .build();

    @Test
    @DisplayName("Should create booking successfully")
    void testAddBookingSuccess() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArguments()[0]);

        BookingResponseDto result = bookingService.addBooking(userId, bookingRequestDto);

        assertNotNull(result);
        assertEquals(bookingRequestDto.getStart(), result.getStart());
        assertEquals(bookingRequestDto.getEnd(), result.getEnd());
    }

    @Test
    @DisplayName("Should throw when user not found")
    void testAddBookingUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> bookingService.addBooking(userId, bookingRequestDto));
    }

    @Test
    @DisplayName("Should throw when item not available")
    void testAddBookingItemNotAvailable() {
        item.setAvailable(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () -> bookingService.addBooking(userId, bookingRequestDto));
    }

    @Test
    @DisplayName("Should approve booking")
    void testApproveBooking() {
        Booking booking = BookingMapper.toBooking(bookingRequestDto, user, item);
        booking.setId(bookingId);
        booking.setStatus(BookingStatus.WAITING);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        BookingResponseDto result = bookingService.approveBooking(owner.getId(), bookingId, true);

        assertEquals(BookingStatus.APPROVED, result.getStatus());
    }

    @Test
    @DisplayName("Should reject booking")
    void testRejectBooking() {
        Booking booking = BookingMapper.toBooking(bookingRequestDto, user, item);
        booking.setId(bookingId);
        booking.setStatus(BookingStatus.WAITING);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        BookingResponseDto result = bookingService.approveBooking(owner.getId(), bookingId, false);

        assertEquals(BookingStatus.REJECTED, result.getStatus());
    }

    @Test
    @DisplayName("Should throw when booking already approved")
    void testApproveBookingConflict() {
        Booking booking = BookingMapper.toBooking(bookingRequestDto, user, item);
        booking.setId(bookingId);
        booking.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        assertThrows(ConflictException.class, () -> bookingService.approveBooking(owner.getId(), bookingId, true));
    }

    @Test
    @DisplayName("Should return booking by id for booker or owner")
    void testGetBookingSuccess() {
        Booking booking = BookingMapper.toBooking(bookingRequestDto, user, item);
        booking.setId(bookingId);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        BookingResponseDto result = bookingService.getBooking(userId, bookingId);

        assertEquals(bookingId, result.getId());
    }

    @Test
    @DisplayName("Should throw when unauthorized user tries to access booking")
    void testGetBookingUnauthorized() {
        Booking booking = BookingMapper.toBooking(bookingRequestDto, user, item);
        booking.setId(bookingId);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        assertThrows(NotFoundException.class, () -> bookingService.getBooking(99L, bookingId));
    }

    @Test
    @DisplayName("Should return all user bookings filtered by state")
    void testGetUserBookings() {
        Booking booking = BookingMapper.toBooking(bookingRequestDto, user, item);
        booking.setId(bookingId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdOrderByStartDesc(userId)).thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getUserBookings(userId, BookingState.ALL.name());
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should return all owner bookings filtered by state")
    void testGetOwnerBookings() {
        Booking booking = BookingMapper.toBooking(bookingRequestDto, user, item);
        booking.setId(bookingId);

        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdOrderByStartDesc(owner.getId())).thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getOwnerBookings(owner.getId(), BookingState.ALL.name());
        assertEquals(1, result.size());
    }
}
