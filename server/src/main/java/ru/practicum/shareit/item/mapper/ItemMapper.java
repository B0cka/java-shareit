package ru.practicum.shareit.item.mapper;

import lombok.RequiredArgsConstructor;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoBooking;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ItemMapper {

    private final BookingMapper bookingMapper;

    public static Item toItem(ItemDto itemDto, Long ownerId) {
        Item item = Item.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .ownerId(ownerId)
                .build();
        return item;
    }

    public static ItemDto toItemDto(Item item) {
        ItemDto itemDto = ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
        return itemDto;
    }

    public static CommentDto mapToDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setText(comment.getDescription());
        dto.setAuthorName(comment.getAuthor().getName());
        dto.setCreated(comment.getCreated());
        return dto;
    }

    public static ItemDtoBooking mapToItemDtoBooking(Item item, List<Booking> bookings, List<Comment> comments) {
        ItemDtoBooking itemDtoBooking = ItemDtoBooking.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .comments(comments.stream().map(CommentDtoMapper::mapToDto).toList())
                .build();
        if (!bookings.isEmpty()) {
            itemDtoBooking.setLastBooking(BookingMapper.toShortDto(bookings.getLast()));
            if (bookings.size() > 1) {
                itemDtoBooking.setNextBooking(BookingMapper.toShortDto(bookings.get(bookings.size() - 2)));
            }
        }
        return itemDtoBooking;
    }

    public static List<ItemDto> toItemDtoList(List<Item> items) {
        return items.stream()
                .map(item -> {
                    ItemDto itemDto = ItemDto.builder()
                            .id(item.getId())
                            .name(item.getName())
                            .description(item.getDescription())
                            .available(item.getAvailable())
                            .build();
                    return itemDto;
                })
                .collect(Collectors.toList());
    }

}
