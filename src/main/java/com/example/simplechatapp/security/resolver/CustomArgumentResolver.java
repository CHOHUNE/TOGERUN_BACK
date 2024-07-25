package com.example.simplechatapp.security.resolver;

import com.example.simplechatapp.dto.UserDTO;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;

@Component
@Log4j2
public class CustomArgumentResolver implements HandlerMethodArgumentResolver {


    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthenticationPrincipal.class) &&
               parameter.getParameterType().equals(UserDTO.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            log.info("principal is UserDetails : {}", principal);
            return principal;
        }
        else if (principal instanceof OAuth2User) {
            log.info("principal is OAuth2User : {}", principal);
            OAuth2User oAuth2User = (OAuth2User) principal;
            // 적절한 방식으로 UserDTO 객체를 생성하여 반환
            return new UserDTO(oAuth2User.getAttribute("email"), "", oAuth2User.getAttribute("nickname"), true, List.of("ROLE_USER"));
        }else{
            log.info("principal is NULL : {}", principal);
        }


        return null;
    }
}
