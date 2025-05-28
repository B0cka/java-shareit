package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {

    BookingResponseDto addBooking(Long userId, BookingRequestDto bookingDto);

    BookingResponseDto approveBooking(Long userId, Long bookingId, boolean approved);

    BookingResponseDto getBooking(Long userId, Long bookingId);

    List<BookingResponseDto> getUserBookings(Long userId, String state);

    List<BookingResponseDto> getOwnerBookings(Long ownerId, String state);
}
