package com.example.simplechatapp.controller;

import com.example.simplechatapp.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserServiceImpl userService;

//    @GetMapping
//    public List<User> getAllUser() {
//        return userService.findAll();
//    }

//    @GetMapping("/{id}")
//    public Optional<User> getUserById(@PathVariable Long id) {
//        return userService.findById(id);
//    }

}
