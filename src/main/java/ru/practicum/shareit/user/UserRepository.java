package ru.practicum.shareit.user;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    List<User> findAll();

    User save(User user);

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

}