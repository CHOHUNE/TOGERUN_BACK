package com.example.simplechatapp.service;


import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.dto.UserModifyDTO;
import com.example.simplechatapp.entity.User;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface UserService {

//    UserDTO getKakaoMember(String accessToken);

    void modifyMember(UserModifyDTO userModifyDTO);


    default UserDTO entityToDTO(User user){

        return new UserDTO(
                    user.getId(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getName(),
                    user.getNickname(),
                    user.isSocial(),
                    user.getGender(),
                    user.getAge(),
                    user.getMobile(),
                    user.getImg(),
                    user.getUserRoleList().stream().map(Enum::name).toList()
            );
    }




}
