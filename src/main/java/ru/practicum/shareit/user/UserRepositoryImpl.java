package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final Map<Long, User> storage = new HashMap<>();
    private long currentId = 1;

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(currentId++);
        }
        storage.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return storage.values().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();
    }

    public void deleteById(Long id) {
        storage.remove(id);
    }
}

