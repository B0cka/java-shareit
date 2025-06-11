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
        Booking booking =  Booking.builder()
                .start(dto.getStart())
                .endTime(dto.getEnd())
                .item(item)
                .booker(booker)
                .build();
        return booking;
    }

    public static BookingResponseDto toBookingResponseDto(Booking booking) {
        BookingResponseDto dto = BookingResponseDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEndTime())
                .status(booking.getStatus())
                .build();

        UserDtoShort booker = UserDtoShort.builder()
                .id(booking.getBooker().getId())
                .build();
        dto.setBooker(booker);

        ItemDtoShort item = ItemDtoShort.builder()
                .id(booking.getItem().getId())
                .name(booking.getItem().getName())
                .build();
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

