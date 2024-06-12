package com.example.simplechatapp;

import com.example.simplechatapp.Service.PostService;
import com.example.simplechatapp.dto.PostDTO;
import com.example.simplechatapp.entity.Post;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

@SpringBootTest
@Log4j2
public class PostServiceTest {

    @Autowired
    PostService postService;

    // service 만 Autowired 하면 휘하에 있는 impl 도 import 된다

    @Test
    @DisplayName("get")
    public void test1() {

        log.info(postService.get(1L));

    }

    @Test
    @DisplayName("register")
    public void test2() {

        PostDTO post = PostDTO.builder().
                title("register test").
                content("register test").
                localDate(LocalDate.now()).
                build();

        postService.register(post);

    }

    @Test
    @DisplayName("modify")
    public void test3() {

            PostDTO post = PostDTO.builder().
                    id(1L).
                    title("modify test").
                    content("modify test").
                    localDate(LocalDate.now()).
                    build();

            postService.modify(post);
    }


}
