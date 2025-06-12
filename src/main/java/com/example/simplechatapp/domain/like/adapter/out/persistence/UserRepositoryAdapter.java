package com.example.simplechatapp.domain.like.adapter.out.persistence;

import com.example.simplechatapp.domain.like.application.port.out.UserPort;
import com.example.simplechatapp.entity.User;
import com.example.simplechatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserPort {

    private final UserRepository userRepository;


    @Override
    public Optional<Long> findUserIdByEmail(String email) {
        return userRepository.findByEmail(email).map(User::getId);
    }

}
