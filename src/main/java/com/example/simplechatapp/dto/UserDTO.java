package com.example.simplechatapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@JsonIgnoreProperties({"authorities", "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "enabled"})
public class UserDTO extends User implements OAuth2User {

    private final Long id;
    private final String email;
    @JsonIgnore
    private final String password;
    private final String name;  // This field is included and will be serialized
    private final String nickname;
    private final boolean social;
    private final String gender;
    private final String age;
    private final String mobile;
    private final String img;
    private List<String> roleNames = new ArrayList<>();

    private boolean isDeleted;
    private LocalDateTime deletedAt;

    public UserDTO(Long id, String email, String password, String name, String nickname, boolean social, String gender, String age, String mobile, String img, List<String> roleNames, boolean isDeleted, LocalDateTime deletedAt) {
        super(
                email != null && !email.isEmpty() ? email : "",
                password != null && !password.isEmpty() ? password : "",
                roleNames != null && !roleNames.isEmpty()
                        ? roleNames.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())
                        : Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        this.id = id;
        this.email = email != null && !email.isEmpty() ? email : "";
        this.password = password != null && !password.isEmpty() ? password : "";
        this.name = name != null && !name.isEmpty() ? name : "";
        this.nickname = nickname != null && !nickname.isEmpty() ? nickname : "";
        this.social = social;
        this.gender = gender != null && !gender.isEmpty() ? gender : "";
        this.age = age != null && !age.isEmpty() ? age : "";
        this.mobile = mobile != null && !mobile.isEmpty() ? mobile : "010-0000-0000";
        this.img = img != null && !img.isEmpty() ? img : "";
        this.roleNames = roleNames != null ? roleNames : new ArrayList<>();
        this.isDeleted = isDeleted;
        this.deletedAt = deletedAt;
    }

    @JsonIgnore
    public Map<String, Object> getClaim() {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("id", id);
        dataMap.put("email", email);
        dataMap.put("nickname", nickname);
        dataMap.put("name", name);

        dataMap.put("social", social);
        dataMap.put("age", age);
        dataMap.put("mobile", mobile);
        dataMap.put("img", img);
        dataMap.put("gender", gender);
        dataMap.put("roleNames", roleNames);
        dataMap.put("isDeleted", isDeleted);
        dataMap.put("deletedAt", deletedAt);

        return dataMap;
    }

    @JsonIgnore
    @Override
    public Map<String, Object> getAttributes() {
        return getClaim();
    }

    @Override
    public String getName() {
        return name;
    }
}
