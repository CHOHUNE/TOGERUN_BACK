package com.example.simplechatapp;

import com.example.simplechatapp.entity.Post;
import com.example.simplechatapp.repository.PostRepository;
import com.example.simplechatapp.util.CustomFileUtil;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@SpringBootTest
@Log4j2

class PostRepoTest {



    @Autowired
    private  PostRepository postRepository;
    @Autowired
    private CustomFileUtil customFileUtil;
    // test 환겨에서는 주로 필드 주입을 사용한다.

    @Test
    @DisplayName("Repository create test")
    public void test1() {

        Assertions.assertNotNull(postRepository);

        log.info(postRepository.getClass().getName());

    }

    @Test
    @DisplayName("post 생성 테스트")
    @Transactional
    @Rollback(value = false)
    public void test2() {

        for (int i = 0; i < 50; i++) {
            Post post = Post.builder().
                    title("제목..." + i).
                    content("내용..." + i).
                    localDate(LocalDate.now()).
                    build();

            postRepository.save(post);
        }
    }

    @Test
    @DisplayName("post 업데이트")
    public void test3() {
        Post post = postRepository.findById(1L).get();
        post.changeTitle("변경된 제목");
        post.changeContent("변경된 내용");
        postRepository.save(post);
    }

    @Test
    @DisplayName("post 페이징")
    public void test4() {


        Pageable pageable = PageRequest.of(0,10, Sort.by("id").descending());

        Page<Post> result = postRepository.findAll(pageable);

        log.info(result.getContent());
        log.info(result.getTotalElements());

    }

    @Test
    @DisplayName("이미지 업로드 테스트")
    public void test5() {

        Post post = Post.builder().title("이미지 저장 게시물").content("이미지 저장 컨텐츠").build();





        post.addImageString(UUID.randomUUID()+"_"+"IMG.JPG");
        post.addImageString(UUID.randomUUID()+"_"+"IMG2.JPG");
        post.addImageString(UUID.randomUUID()+"_"+"IMG3.JPG");

        postRepository.save(post);

    }


//    @Test
//    @DisplayName("querydsl 테스트")
//    public void test5() {
//        postRepository.search1();
//
//    }




}
