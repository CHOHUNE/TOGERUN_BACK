package com.example.simplechatapp.Service;

import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.dto.UserModifyDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{


    @Override
    public UserDTO getKakaoMember(String accessToken) {
        return null;
    }

    @Override
    public void modifyMember(UserModifyDTO userModifyDTO) {

    }
}
