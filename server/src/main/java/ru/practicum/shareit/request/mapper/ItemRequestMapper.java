package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

public class ItemRequestMapper {

    public static ItemRequest toItemRequest(ItemRequestCreateDto itemRequestCreateDto, User requestor){
        ItemRequest itemRequest = ItemRequest.builder()
                .description(itemRequestCreateDto.getDescription())
                .created(LocalDateTime.now())
                .requestor(requestor)
                .build();
        return itemRequest;
    }

}
