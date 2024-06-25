package com.example.simplechatapp.dto.oauth2;

import com.example.simplechatapp.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final UserDTO userDTO;


    //attributes 란? OAuth2 공급자로부터 받은 사용자 정보를 담고 있는 Map
    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return userDTO.getRoleNames().toString();
            }
        });
        return collection;
    }

    //Authorities : 해당 사용자가 가지고 있는 권한 목록을 리턴


    @Override
    public String getName() {
        return userDTO.getUsername();
    }

    public String getEmail() {
        return userDTO.getEmail();
    }

    public String getNickname() {
        return userDTO.getNickname();
    }

    public boolean isSocial() {
        return userDTO.isSocial();
    }

    public List<String> getRoleNames() {
        return userDTO.getRoleNames();
    }


}
