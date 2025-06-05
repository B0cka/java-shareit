package ru.practicum.shareit.request.controller;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {

    ItemRequestService itemRequestService;

    @PostMapping()
    public ItemRequestDto postRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                      @RequestBody ItemRequestCreateDto itemRequestCreateDto){
        return itemRequestService.createRequest(userId, itemRequestCreateDto);
    }

    @GetMapping()
    public List<ItemRequestDto> getRequests(@RequestHeader("X-Sharer-User-Id") Long userId){
        return itemRequestService.getRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId){
        return itemRequestService.getAllRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestsById(@PathVariable Long requestId){
        return itemRequestService.getRequestsById(requestId);
    }
}
