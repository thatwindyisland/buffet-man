package org.example.buffet.mapper;

import org.example.buffet.entity.User;
import java.util.List;

public interface UserMapper {
    User findById(Long id);
    User findByUsername(String username);
    List<User> findAll();
    int insert(User user);
    int update(User user);
    int deleteById(Long id);
}
