package com.example.simplechatapp.controller;

import com.example.simplechatapp.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log4j2
@RequiredArgsConstructor
public class SocialController {

    private final UserService userService;





}


 // TokenHeader { Payload : Signature}
// 헤더에는 토큰 타입, 해싱 알고리즘으로 구성되어 있으며 일반적으로 JSON 객체로 표현된다.
// 페이로드는 토큰이 실제로 전달하려는 값이며 클레임이라는 키-값 쌍으로 구성된다.
// 시그니처는 헤더와 페이로드를 합쳐 해싱 알고리즘을 적용하여 생성되며 토큰이 변조되지 않았음을 증명한다.
