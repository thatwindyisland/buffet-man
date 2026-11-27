package org.example.buffet.controller;
import org.example.buffet.service.UserService;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.example.buffet.mapper.UserMapper;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping("/api/user")
    public Map<String,Serializable> queryUser(
            @RequestParam(name = "method",defaultValue = "id")
            String method,
            @RequestParam(name = "value",defaultValue = "?")
            Object value
    ){
        if (value == "?"){
            return Map.of("Error","No query value");
        }
        if (Objects.equals(method, "id")){
            return Map.of(
                    "id", (Serializable) value,
                    "userName", userService.getUserById((Long) value).getUsername(),
                    "displayName", userService.getUserById((Long) value).getDisplayName(),
                    "enabled", userService.getUserById((Long) value).isEnabled(),
                    "role",userService.getUserById((Long) value).getRole()
            );
        }
        return Map.of("Error","Method isn't supported.");
    }
}
