package ru.practicum.shareit.request.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestCreateDto {
    private String description;
}

