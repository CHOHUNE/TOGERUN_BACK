package com.example.simplechatapp.service;


import com.example.simplechatapp.entity.User;
import com.example.simplechatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserFindService {

    private final UserRepository userRepository;

    public User findUser(String email) {
        return userRepository.findById(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
