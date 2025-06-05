package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.ItemDtoShort;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDtoShort;

public class BookingMapper {

    public static Booking toBooking(BookingRequestDto dto, User booker, Item item) {
        Booking booking = new Booking();
        booking.setStart(dto.getStart());
        booking.setEndTime(dto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        return booking;
    }

    public static BookingResponseDto toBookingResponseDto(Booking booking) {
        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEndTime());
        dto.setStatus(booking.getStatus());

        UserDtoShort booker = new UserDtoShort();
        booker.setId(booking.getBooker().getId());
        dto.setBooker(booker);

        ItemDtoShort item = new ItemDtoShort();
        item.setId(booking.getItem().getId());
        item.setName(booking.getItem().getName());
        dto.setItem(item);

        return dto;
    }

    public static BookingShortDto toShortDto(Booking booking) {
        BookingShortDto dto = new BookingShortDto();
        dto.setId(booking.getId());
        dto.setBookerId(booking.getBooker().getId());
        return dto;
    }
}

