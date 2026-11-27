package org.example.buffet.mapper;

import org.example.buffet.entity.User;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class MemoryUserMapper implements UserMapper {
    private final Map<Long, User> users = new HashMap<>();
    Long nextId = 1L;

    @Override
    public User findById(Long id) {
        return users.get(id);
    }

    @Override
    public User findByUsername(String username) {
        return users.values().stream().filter(user -> {
            return Objects.equals(user.getUsername(), username);
        }).findFirst().orElse(null);
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public int insert(User user) {
        user.setId(nextId++);
        users.put(user.getId(),user);
        return 1;
    }

    @Override
    public int update(User user) {
        if(users.containsKey(user.getId())){
            return 0;
        }
        users.put(user.getId(),user);
        return 1;
    }

    @Override
    public int deleteById(Long id) {
        return users.remove(id) != null ? 1 : 0;
    }
}
