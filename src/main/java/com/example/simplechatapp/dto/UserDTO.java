package com.example.simplechatapp.dto;

import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



@Getter
public class UserDTO extends User  implements  OAuth2User {


    private final Long id;
    private final String email;
    private final String pw;
    private final String name;
    private final String nickname;
    private final boolean social;

    private final String gender;
    private final String age;
    private final String mobile;
    private final String img;

    private List<String> roleNames = new ArrayList<>();



    public UserDTO(Long id, String email, String password, String name, String nickname, boolean social, String gender, String age, String mobile, String img, List<String> roleNames) {

        super(email, password, roleNames.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));

        this.id = id;
        this.email = email;
        this.pw = password;
        this.name = name;
        this.social=social;
        this.nickname = nickname;
        this.gender = gender;
        this.age = age;
        this.mobile = mobile;
        this.img = img;
        this.roleNames = roleNames;

    }

    public Map<String, Object> getClaim() {
        Map<String, Object> dataMap = new HashMap<>();

        dataMap.put("id", id);
        dataMap.put("email", email);
        dataMap.put("pw", pw);
        dataMap.put("nickname", nickname);
        dataMap.put("social", social);

        dataMap.put("name", name);
        dataMap.put("age", age);
        dataMap.put("mobile", mobile);
        dataMap.put("img", img);
        dataMap.put("gender", gender);

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
