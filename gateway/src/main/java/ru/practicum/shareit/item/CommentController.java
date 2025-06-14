package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class CommentController {
    private final CommentClient commentClient;

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @PathVariable long itemId,
                                             @RequestBody CommentDto commentDto) {
        return commentClient.addComment(userId, itemId, commentDto);
    }

    @GetMapping("/comments")
    public ResponseEntity<Object> getComments(@RequestHeader("X-Sharer-User-Id") long userId) {
        return commentClient.getComments(userId);
    }
}