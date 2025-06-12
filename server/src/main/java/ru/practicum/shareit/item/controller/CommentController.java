package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {
    private final ItemService itemService;

    @PostMapping("/items/{itemId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@RequestHeader("X-Sharer-User-Id") long userId,
                                 @PathVariable long itemId,
                                 @RequestBody CommentDto commentDto) {
        return itemService.addComment(userId, itemId, commentDto);
    }

    @GetMapping("/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Comment> getComments(@RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.getComments(userId);
    }

}