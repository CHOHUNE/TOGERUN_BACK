package com.example.simplechatapp.dto;
import lombok.Data;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;
import java.util.stream.Collectors;



@Getter
public class UserDTO extends User  implements  OAuth2User {


    private final Long id;
    private final String email;
    private final String pw;
    private final String nickname;
    private final boolean social;

    private List<String> roleNames = new ArrayList<>();



    public UserDTO(Long id, String email, String password, String nickname, boolean social, List<String> roleNames) {

        super(email, password, roleNames.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));

        this.id = id;
        this.email = email;
        this.pw = password;
        this.social=social;
        this.nickname = nickname;
        this.roleNames = roleNames;

    }

    public Map<String, Object> getClaim() {
        Map<String, Object> dataMap = new HashMap<>();

        dataMap.put("id", id);
        dataMap.put("email", email);
        dataMap.put("pw", pw);
        dataMap.put("nickname", nickname);
        dataMap.put("social", social);
        dataMap.put("roleNames", roleNames);

        return dataMap;
    }


    @Override
    public Map<String, Object> getAttributes() {
        return getClaim();
    }

    @Override
    public String getName() {
        return email;
    }
}
