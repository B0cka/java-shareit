package ru.practicum.shareit.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    @Override
    public List<User> getAllUsers() {
        log.info("Запрос на всех пользователей");
        return repository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        log.info("Запрос на пользователя с id {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: id=" + id));
    }

    @Override
    @Transactional
    public User saveUser(User user) {
        log.info("Запрос на создание пользователя");
        validate(user);

        Optional<User> existingUser = repository.findAll().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(user.getEmail()))
                .findFirst();

        if (existingUser.isPresent()) {
            throw new ConflictException("Email уже используется");
        }

        return repository.save(user);
    }

    @Override
    @Transactional
    public User updateUser(Long id, UserDto userDto) {
        log.info("Запрос на обновление пользователя id={}", id);
        User existingUser = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: id=" + id));

        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            Optional<User> userWithEmail = repository.findByEmail(userDto.getEmail());
            if (userWithEmail.isPresent() && !userWithEmail.get().getId().equals(id)) {
                throw new ConflictException("Email is already taken by another user");
            }
            existingUser.setEmail(userDto.getEmail());
        }

        if (userDto.getEmail() != null) {
            existingUser.setEmail(userDto.getEmail());
        }
        return repository.save(existingUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Запрос на удаление пользователя id={}", id);
        repository.deleteById(id);
    }

    public void validate(User user) {
        if (user.getEmail() == null) {
            throw new ValidationException("Email not found");
        }
        if (!user.getEmail().contains("@")) {
            throw new ValidationException("Email must be with @");
        }
    }

}
