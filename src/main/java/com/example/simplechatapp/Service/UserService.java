package com.example.simplechatapp.Service;


import com.example.simplechatapp.dto.UserModifyDTO;
import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.entity.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Transactional
public interface UserService {

    UserDTO getKakaoMember(String accessToken);

    void modifyMember(UserModifyDTO userModifyDTO);


    default UserDTO entityToDTO(User user){

            UserDTO dto = new UserDTO(
                    user.getEmail(),
                    user.getPassword(),
                    user.getNickname(),
                    user.isSocial(),
                    user.getUserRoleList().stream().map(Enum::name).toList());

            return dto;
    }


}
