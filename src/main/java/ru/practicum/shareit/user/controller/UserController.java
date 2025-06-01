package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable("id") Long id) {
        return UserMapper.toUserDto(userService.getUserById(id));
    }

    @PostMapping
    public UserDto saveNewUser(@RequestBody UserDto userDto) {
        return UserMapper.toUserDto(userService.saveUser(UserMapper.toUser(userDto)));
    }

    @PatchMapping("/{id}")
    public UserDto updateUser(@PathVariable("id") Long id, @RequestBody UserDto userDto) {
        return UserMapper.toUserDto(userService.updateUser(id, userDto));
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
    }
}
