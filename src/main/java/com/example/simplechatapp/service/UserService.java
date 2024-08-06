package com.example.simplechatapp.service;


import com.example.simplechatapp.dto.UserModifyDTO;
import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.entity.User;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface UserService {

    UserDTO getKakaoMember(String accessToken);

    void modifyMember(UserModifyDTO userModifyDTO);


    default UserDTO entityToDTO(User user){

            UserDTO dto = new UserDTO(
                    user.getId(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getNickname(),
                    user.isSocial(),
                    user.getUserRoleList().stream().map(Enum::name).toList());

            return dto;
    }




}
