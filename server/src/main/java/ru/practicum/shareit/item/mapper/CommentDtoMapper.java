package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;

public class CommentDtoMapper {

    static CommentDto mapToDto(Comment comment) {
        CommentDto dto = CommentDto.builder()
                .id(comment.getId())
                .text(comment.getDescription())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
        return dto;
    }

}
