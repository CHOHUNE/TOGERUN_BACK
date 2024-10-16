package com.example.simplechatapp.service;

import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.dto.UserModifyDTO;
import com.example.simplechatapp.entity.User;
import com.example.simplechatapp.entity.UserRole;
import com.example.simplechatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;


@Service
@Log4j2
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {


    private final UserRepository userRepository;

    private static final int MIN_NICKNAME_LENGTH = 3;
    private static final int MAX_NICKNAME_LENGTH = 20;
    private static final Pattern VALID_NICKNAME_REGEX = Pattern.compile("^[a-zA-Z0-9가-힣_-]+$");
    private static final List<String> FORBIDDEN_NICKNAMES = Arrays.asList("admin", "root", "system");

    @Override
    public UserDTO modifyMember(UserDTO currentUser, UserModifyDTO userModifyDTO) {

        User user = userRepository.findByEmail(currentUser.getEmail())
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        user.changeNickname(userModifyDTO.getNickname());
        user.setAge(userModifyDTO.getAge());
        user.setGender(userModifyDTO.getGender());
        user.setMobile(userModifyDTO.getMobile());
        user.setDeleted(false);
        user.setDeletedAt(null);
        user.changeSocial(false);

//        user.changePw(passwordEncoder.encode(userModifyDTO.getPw()));
//       일반 로그인 기능 폐기로 인한 주석 처리

        List<UserRole> currentRoles = user.getUserRoleList();

        if (currentRoles.contains(UserRole.ROLE_BRONZE)|| currentRoles.isEmpty()) {

            log.info("CATCH BRONZE:{}", user.getUserRoleList());
            user.getUserRoleList().clear();
            user.getUserRoleList().add(UserRole.ROLE_SILVER);
        }

        User savedUser = userRepository.save(user);
        updateSecurityContext(user);

        return entityToDTO(savedUser);
    }


    @Override
    public UserDTO getMember(String email) {

        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User Not Found"));
        return entityToDTO(user);
    }


    @Override
    public void softDeleteUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User Not Found"));

        user.softDelete();
        userRepository.save(user);
    }

    @Override
    public UserDTO restoreUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User Not Found"));

        user.restore();

        return entityToDTO(userRepository.save(user));
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAllUsers().stream()
                .map(this::entityToDTO)
                .toList();
    }

    public boolean isNicknameAvailable(String nickname) {

        if (nickname == null || nickname.trim().isEmpty()) {
            return false;
        }

        // 길이 검사
        if (nickname.length() < MIN_NICKNAME_LENGTH || nickname.length() > MAX_NICKNAME_LENGTH) {
            return false;
        }

        // 문자 유효성 검사
        if (!VALID_NICKNAME_REGEX.matcher(nickname).matches()) {
            return false;
        }

        // 금지된 닉네임 검사
        if (FORBIDDEN_NICKNAMES.contains(nickname.toLowerCase())) {
            return false;
        }

        // 중복 검사
        Optional<User> existingUser = userRepository.findByNickname(nickname);

        return existingUser.isEmpty();
    }

    private void updateSecurityContext(User user) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDTO updatedPrincipal = entityToDTO(user);

        List<SimpleGrantedAuthority> updatedAuthorities = user.getUserRoleList().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .toList();

        Authentication newAuth = new UsernamePasswordAuthenticationToken(updatedPrincipal, auth.getCredentials(), updatedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        log.info("Security Context Updated:{}", newAuth);
    }

}
