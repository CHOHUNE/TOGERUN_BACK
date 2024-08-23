//package com.example.simplechatapp.util;
//
//import jakarta.annotation.PostConstruct;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.log4j.Log4j2;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.io.FileSystemResource;
//import org.springframework.core.io.Resource;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Component;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//@Log4j2
//@RequiredArgsConstructor
//@Component
//public class CustomFileUtil {
//
//
//
//    @Value("${com.example.simplechatapp.upload.path}")
//    private String uploadPath;
//
//    @PostConstruct
//    // 빈(컴포넌트)이 주입될 때 한 번만 실행되는 메서드라 생각하면 편리
//    // 주로 디렉토리 생성이나 설정 값 초기화 등의 작업을 포함한다.
//    public void init() {
//        File tempFolder = new File(uploadPath);
//
//        if(!tempFolder.exists()) tempFolder.mkdir();
//        //mkdir : 디렉토리 생성
//
//        uploadPath = tempFolder.getAbsolutePath();
//        //getAbsolutePath : 파일의 절대 경로를 반환
//
//    }
//
//    public List<String > saveFiles(List<MultipartFile> files ) throws RuntimeException {
//        if(files == null || files.isEmpty()) return null;
//
//        List<String> uploadNames =  new ArrayList<>();
//
//        for (MultipartFile multipartFile : files) {
//            String savedName = UUID.randomUUID().toString() + "_" + multipartFile.getOriginalFilename();
//            Path savePath = Paths.get(uploadPath, savedName);
//            //savePath 설명 : 파일을 저장할 경로를 만든다.
//            //Paths.get 역할 : 파일 시스템에 대한 경로를 나타내는 Path 객체를 반환한다.
//
//            try{
//
//                Files.copy(multipartFile.getInputStream(), savePath);
//                String contentType = multipartFile.getContentType();
//
//                uploadNames.add(savedName);
//
//
//            }catch (IOException e){
//                throw new RuntimeException(e.getMessage());
//            }
//        }
//
//        return uploadNames;
//    }
//
//    public ResponseEntity <Resource> getFile(String fileName) throws IOException {
//
//        Resource resource = new FileSystemResource(uploadPath + File.separator + fileName);
//        // FileSystemResource : 파일 시스템의 리소스를 나타내는 Resource 구현체 ( 파일, 디렉토리, URL 등)
//
//
//        HttpHeaders httpHeaders = new HttpHeaders();
//
//        httpHeaders.add("Content-Type",Files.probeContentType(resource.getFile().toPath()));
//        //헤더를 추가하는 이유 : 파일의 종류에 따라 다른 헤더를 추가해야 하는 경우가 있기 때문
//        // File.probeContentType : 파일의 MIME 타입을 반환 예를 들면, 이미지 파일인 경우 image/jpeg, 텍스트 파일인 경우 text/plain
//
//
//        return  ResponseEntity.ok().headers(httpHeaders).body(resource);
//        // 자원을 바디에, 타입에 맞는 타입을 헤더에 추가해서 반환 한다.
//
//    }
//
//    //deleteFile 흐름 : 1. 파일 이름을 받아서 2. 파일 경로를 만들고 3. 파일을 삭제한다.
//    public void deleteFile(List<String> fileNames) {
//        File file = new File( uploadPath + File.separator + fileNames);
//
//        // File.separator : 파일 경로 구분자 "/"
//
//        if(fileNames ==null || fileNames.isEmpty()) return;
//
//        fileNames.forEach( fileName->{
//
//            Path filePath = Paths.get(uploadPath, fileName);
//
//            try {
//                Files.deleteIfExists(filePath);
//
//            } catch (IOException e) {
//
//                throw new RuntimeException(e);
//            }
//        });
//    }
//
//
//
//}
