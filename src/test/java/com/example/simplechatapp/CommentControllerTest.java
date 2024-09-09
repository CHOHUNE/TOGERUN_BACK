package com.example.simplechatapp;


import com.example.simplechatapp.controller.CommentController;
import com.example.simplechatapp.dto.CommentRequestDto;
import com.example.simplechatapp.dto.CommentResponseDto;
import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.service.CommentService;
import com.example.simplechatapp.util.CustomException;
import com.example.simplechatapp.util.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.util.JpaMetamodel;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(CommentController.class)
@MockBean(JpaMetamodel.class)
//JpaMetamodel : JPA 엔티티의 메타모델을 사용할 수 있게 해주는 클래스
// 메타모델 : 엔티티 클래스의 정보를 담고 있는 클래스
public class CommentControllerTest {

    @MockBean
    CommentService commentService;

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @Test
    @DisplayName("댓글 생성 컨트롤러 테스트")
    @WithMockUser //JWTFilter 를 제외시키기 위한 MockUser
    void createSnsCommentTest() throws Exception{
        //given
        CommentRequestDto commentRequestDto = CommentRequestDto.builder()
                .post_id(1L)
                .content("댓글 작성 테스트")
                .createdBy("Lee")
                .build();
        String stringJson = createStringJson(commentRequestDto);

        CommentResponseDto snsCommentResponseDto = CommentResponseDto.builder()
                .id(1L)
                .content("댓글 작성 테스트")
                .createdBy("Lee")
                .build();

        UserDTO principal = new UserDTO(1L, "email", "password", "nickname", false, List.of());


        given(commentService.createComment(commentRequestDto, principal)).willReturn(snsCommentResponseDto);
        String expectedJson = createStringJson(snsCommentResponseDto);
        //then
        mvc.perform(post("/api/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stringJson).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson))

                .andDo(print());
    }

    public String createStringJson(Object dto) throws JsonProcessingException {
        return mapper.writeValueAsString(dto);

    }

    @Test
    @DisplayName("댓글 조회 컨트롤러 테스트")
    @WithMockUser
    void getCommentTest() throws Exception {

        CommentResponseDto parentComment = CommentResponseDto.builder()
                .id(1L)
                .postId(1L)
                .content("댓글 작성 테스트")
                .createdBy("Lee")
                .build();

        CommentResponseDto childComment = CommentResponseDto.builder()
                .id(2L)
                .postId(1L)
                .content("대댓글 작성 테스트2")
                .createdBy("Lee")
                .build();

        List<CommentResponseDto> commentList = new ArrayList<>();

        commentList.add(parentComment);
        commentList.add(childComment);

        String expectedJson= createStringJson(commentList);

        given(commentService.findCommentListByPostId(1L)).willReturn(commentList);
        //then

        mvc.perform(get("/api/comment/{postId}", 1L).with(csrf()))
                .andExpect(content().json(expectedJson))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("댓글 생성 컨트롤러 예외 테스트 - 댓글 작성시 해당 게시글이 없을 경우")
    @WithMockUser
    void createCommentExceptionTest() throws Exception{
        //given

        CommentRequestDto commentRequestDto = CommentRequestDto.builder()
                .post_id(1L)
                .content("댓글 작성 테스트")
                .createdBy("Lee")
                .build();

        String stringJson = createStringJson(commentRequestDto);


        UserDTO principal = new UserDTO(1L, "email", "password", "nickname", false, List.of());


        given(commentService.createComment(any(CommentRequestDto.class), principal))
                .willThrow(new CustomException(ErrorCode.POST_NOT_FOUND));

        mvc.perform(post("/api/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stringJson).with(csrf()))
                .andExpect(status().is(ErrorCode.POST_NOT_FOUND.getHttpStatus().value()))
                .andExpect(jsonPath("$.message").value(ErrorCode.POST_NOT_FOUND.getDetail()))
                .andDo(print());
    }


    @Test
    @DisplayName("댓글 삭제 컨트롤러 테스트")
    @WithMockUser
    void deleteCommentTest()throws Exception {

        mvc.perform(delete("/api/comment/{commentId}", 1L)
                        .with(csrf()))  // CSRF 토큰을 포함합니다.
                .andExpect(status().isOk())
                .andDo(print());

    }
}

/*
SpringBootTest 는 전체 애플리케이션을 테스트하는 통합 테스트에 사용 된다.
실제 애플리케이션 환경과 동일하게 모든 빈이 로드되며 실제 데이터베이스 연결도 할수 있다.

@WebMvcTest 와 @MockBean 은 웹 계층(컨트롤러) 테스트에 집중한다.
컨트롤러, 필터, 컨트롤러 어드바이스를 테스트하는데 사용되며 웹 계층 이외의 빈들은 로드되지 않는다

단위테스트나 웹계층 테스트는 WebMvcTest 를 쓰고 통합 테스트엔 SpringBootTest 를 쓰는게 좋다.
*/

// 파일 내에서 구동 되는 자동화 테스트 (Junit, Mockito, MockMvc)
// CI/CD 파이프라인에서 자동 실행 되어 코드 품질을 유지할 수 있다.
// 수동테스트인 Postman 과 같이 쓰이는 경우가 일반적이다.