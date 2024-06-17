package com.example.simplechatapp.util;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

@Log4j2
@RequiredArgsConstructor
@Component
public class CustomFileUtil {



    @Value("${com.example.simplechatapp.upload.path}")
    private String uploadPath;

    @PostConstruct
    // 빈(컴포넌트)이 주입될 때 한 번만 실행되는 메서드라 생각하면 편리
    // 주로 디렉토리 생성이나 설정 값 초기화 등의 작업을 포함한다.
    public void init() {
        File tempFolder = new File(uploadPath);

        if(!tempFolder.exists()) tempFolder.mkdir();
        //mkdir : 디렉토리 생성

        uploadPath = tempFolder.getAbsolutePath();
        //getAbsolutePath : 파일의 절대 경로를 반환

    }

}
