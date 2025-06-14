package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;


@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private final RequestClient requestClient;

    @PostMapping()
    public ResponseEntity<Object> postRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @RequestBody ItemRequestCreateDto itemRequestCreateDto) {
        return requestClient.createRequest(userId, itemRequestCreateDto);
    }

    @GetMapping()
    public ResponseEntity<Object> getRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return requestClient.getRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return requestClient.getAllRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestsById(@PathVariable Long requestId) {
        return requestClient.getRequestsById(requestId);
    }
}
