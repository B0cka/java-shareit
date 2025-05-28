package ru.practicum.shareit.booking.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingResponseDto addBooking(Long userId, BookingRequestDto bookingDto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found with id: " + bookingDto.getItemId()));

        if(!item.getAvailable()){
            throw new ValidationException("item must be available!");
        }

        if(bookingDto.getEnd().isBefore(LocalDateTime.now())){
            throw new ValidationException("end can not be in the past!");
        }

        if(bookingDto.getStart().equals(bookingDto.getEnd())){
            throw new ValidationException("start can not be in the past!");
        }

        if(bookingDto.getStart().isBefore(LocalDateTime.now())){
            throw new ValidationException("start can not be in the past!");
        }

        Booking booking = BookingMapper.toBooking(bookingDto, user, item);
        booking.setStatus(BookingStatus.WAITING);

        Booking saved = bookingRepository.save(booking);
        return BookingMapper.toBookingResponseDto(saved);
    }

    @Override
    public BookingResponseDto approveBooking(Long userId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + bookingId));

        Item item = booking.getItem();

        if (!item.getOwnerId().equals(userId)) {
            throw new ValidationException("Only the owner of the item can approve the booking.");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ConflictException("Booking is already " + booking.getStatus());
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        bookingRepository.save(booking);
        return BookingMapper.toBookingResponseDto(booking);
    }


    @Override
    public BookingResponseDto getBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + bookingId));

        Item item = booking.getItem();

        if (!(item.getOwnerId().equals(userId) || booking.getBooker().getId().equals(userId))) {
            throw new NotFoundException("Only owner or booker can view this booking.");
        }

        return BookingMapper.toBookingResponseDto(booking);
    }


    @Override
    public List<BookingResponseDto> getUserBookings(Long userId, String stateStr) {

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        BookingState state = BookingState.from(stateStr);

        List<Booking> bookings = bookingRepository.findByBookerIdOrderByStartDesc(userId);

        LocalDateTime now = LocalDateTime.now();

        return bookings.stream()
                .filter(booking -> {
                    switch (state) {
                        case ALL:
                            return true;
                        case CURRENT:
                            return booking.getStart().isBefore(now) && booking.getEndTime().isAfter(now);
                        case PAST:
                            return booking.getEndTime().isBefore(now);
                        case FUTURE:
                            return booking.getStart().isAfter(now);
                        case WAITING:
                            return booking.getStatus() == BookingStatus.WAITING;
                        case REJECTED:
                            return booking.getStatus() == BookingStatus.REJECTED;
                        default:
                            return false;
                    }
                })
                .map(BookingMapper::toBookingResponseDto)
                .toList();
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long ownerId, String stateStr) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        BookingState state = BookingState.from(stateStr);
        List<Booking> bookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(ownerId);
        LocalDateTime now = LocalDateTime.now();

        return bookings.stream()
                .filter(booking -> {
                    switch (state) {
                        case ALL:
                            return true;
                        case CURRENT:
                            return booking.getStart().isBefore(now) && booking.getEndTime().isAfter(now);
                        case PAST:
                            return booking.getEndTime().isBefore(now);
                        case FUTURE:
                            return booking.getStart().isAfter(now);
                        case WAITING:
                            return booking.getStatus() == BookingStatus.WAITING;
                        case REJECTED:
                            return booking.getStatus() == BookingStatus.REJECTED;
                        default:
                            return false;
                    }
                })
                .map(BookingMapper::toBookingResponseDto)
                .toList();
    }

}