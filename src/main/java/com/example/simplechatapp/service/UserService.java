package com.example.simplechatapp.service;


import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.dto.UserModifyDTO;
import com.example.simplechatapp.entity.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface UserService {

//    UserDTO getKakaoMember(String accessToken);

    UserDTO modifyMember(UserDTO userDTO,UserModifyDTO userModifyDTO);

    UserDTO getMember(String email);

    void softDeleteUser(Long userId);

    UserDTO restoreUser(Long userId);

    List<UserDTO> getAllUsers();

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
                    user.getUserRoleList().stream().map(Enum::name).toList(),
                    user.isDeleted(),
                    user.getDeletedAt()

            );}





}
