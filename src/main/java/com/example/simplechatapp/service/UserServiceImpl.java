package com.example.simplechatapp.service;

import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.dto.UserModifyDTO;
import com.example.simplechatapp.entity.User;
import com.example.simplechatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Log4j2
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Override
    public void modifyMember(UserModifyDTO userModifyDTO) {

        User user = userRepository.findByEmail(userModifyDTO.getEmail()).orElseThrow(()->new RuntimeException("User Not Found"));

        user.changeNickname(userModifyDTO.getNickname());
        user.setAge(userModifyDTO.getAge());
        user.setGender(userModifyDTO.getGender());
        user.setMobile(userModifyDTO.getMobile());
        user.changeSocial(false);
//        user.changePw(passwordEncoder.encode(userModifyDTO.getPw()));

        userRepository.save(user);

    }

    @Override
    public UserDTO getMember(String email) {

        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User Not Found"));
        return entityToDTO(user);
    }



    @Override
    public void softDeleteUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User Not Found"));

        user.softDelete();
        userRepository.save(user);
    }

    @Override
    public UserDTO restoreUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User Not Found"));

        user.restore();

        return entityToDTO(userRepository.save(user));
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAllUsers().stream()
                .map(this::entityToDTO)
                .toList();
    }
}
