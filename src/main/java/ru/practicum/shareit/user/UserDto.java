package ru.practicum.shareit.user;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String name;
    private String email;
}
