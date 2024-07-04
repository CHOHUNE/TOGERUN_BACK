package com.example.simplechatapp.dto;
import lombok.Data;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.*;
import java.util.stream.Collectors;


public class UserDTO extends User {


    private final String email;
    private final String pw;
    private final String nickname;

    private boolean social;

    private List<String> roleNames = new ArrayList<>();



    public UserDTO(String email, String password, String nickname, boolean social, List<String> roleNames) {

        super(email, password, roleNames.stream().map(str-> new SimpleGrantedAuthority("ROLE_" + str)).collect(Collectors.toList()));

        this.email = email;
        this.pw = password;
        this.social=social;
        this.nickname = nickname;
        this.roleNames = roleNames;

    }

    public Map<String, Object> getClaim() {
        Map<String, Object> dataMap = new HashMap<>();

        dataMap.put("email", email);
        dataMap.put("pw", pw);
        dataMap.put("nickname", nickname);
        dataMap.put("social", social);
        dataMap.put("roleNames", roleNames);

        return dataMap;
    }

    public String getEmail() {
        return email;
    }

    public String getPw() {
        return pw;
    }

    public String getNickname() {
        return nickname;
    }

    public boolean isSocial() {
        return social;
    }

    public List<String> getRoleNames() {
        return roleNames;
    }



}
