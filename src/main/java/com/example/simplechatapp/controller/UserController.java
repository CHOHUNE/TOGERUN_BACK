package com.example.simplechatapp.controller;

import com.example.simplechatapp.Service.UserServiceImpl;
import com.example.simplechatapp.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
