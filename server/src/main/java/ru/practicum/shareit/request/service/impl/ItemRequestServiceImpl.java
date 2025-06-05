package ru.practicum.shareit.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {

    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestDto createRequest(Long userId, ItemRequestCreateDto responseDto){
        log.info("Create user with id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User NOT FOUND"));

        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(responseDto, user);

        itemRequestRepository.save(itemRequest);

        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .items(List.of())
                .build();

        return itemRequestDto;
    }

    @Override
    public List<ItemRequestDto> getRequests(Long userId) {
        log.info("Get requests by user id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User NOT FOUND"));

        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId);

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        List<Item> items = itemRepository.findAllByRequestIdIn(requestIds);

        Map<Long, List<Item>> itemsByRequestId = items.stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        return requests.stream()
                .map(request -> ItemRequestDto.builder()
                        .id(request.getId())
                        .description(request.getDescription())
                        .created(request.getCreated())
                        .items(
                                ItemMapper.toItemDtoList(
                                        itemsByRequestId.getOrDefault(request.getId(), List.of())
                                )
                        )
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId){
        log.info("Get ALL requests");

        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId);

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        List<Item> items = itemRepository.findAllByRequestIdIn(requestIds);

        Map<Long, List<Item>> itemsByRequestId = items.stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        return requests.stream()
                .map(request -> ItemRequestDto.builder()
                        .id(request.getId())
                        .description(request.getDescription())
                        .created(request.getCreated())
                        .items(
                                ItemMapper.toItemDtoList(
                                        itemsByRequestId.getOrDefault(request.getId(), List.of())
                                )
                        )
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getRequestsById(Long requestId) {
        log.info("Get request by id: {}", requestId);

        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id: " + requestId + " NOT FOUND"));

        List<Item> items = itemRepository.findAllByRequestId(request.getId());

        List<ItemDto> itemDtos = ItemMapper.toItemDtoList(items);

        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(itemDtos)
                .build();
    }

}
