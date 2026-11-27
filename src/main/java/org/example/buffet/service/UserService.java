package org.example.buffet.service;

import org.example.buffet.entity.Role;
import org.example.buffet.entity.User;
import org.example.buffet.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public List<User> getAllUsers() {
        return this.userMapper.findAll();
    }

    public User getUserById(Long id) {
        return this.userMapper.findById(id);
    }

    public void createUser(User user) {
        if (userMapper.findByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("User has already existed.");
        }
        this.userMapper.insert(user);
    }

    public List<User> getAllMerchant() {
        List<User> users = getAllUsers();
        List<User> merchants = new java.util.ArrayList<>(List.of());
        for (User user : users) {
            if (user.getRole() == Role.MERCHANT) {
                merchants.add(user);
            }
        }
        return merchants;
    }

    public void disableById(Long id) {
        if (userMapper.findById(id) == null) {
            throw new IllegalArgumentException("No user found");
        } else if (userMapper.findById(id).getRole() == Role.MERCHANT) {
            throw new IllegalArgumentException("Only merchants can be disabled.");
        }
        User user = userMapper.findById(id);
        user.setEnabled(false);
        userMapper.update(user);
    }

    public void deleteMerchant(Long id) {
        if (userMapper.findById(id) == null) {
            throw new IllegalArgumentException("No user found");
        } else if (userMapper.findById(id).getRole() == Role.MERCHANT) {
            throw new IllegalArgumentException("Only merchants can be disabled.");
        }
        userMapper.deleteById(id);
    }
}
