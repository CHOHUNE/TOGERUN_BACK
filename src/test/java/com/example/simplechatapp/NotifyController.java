package com.example.simplechatapp;

import com.example.simplechatapp.controller.NotifyController;
import com.example.simplechatapp.dto.NotifyDto;
import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.service.NotifyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotifyController.class)
class NotifyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotifyService notifyService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        userDTO = new UserDTO(1L, "test@example.com", "password", "nickname", false, List.of());

    }

    @Test
    @WithMockUser
    void testSubscribe() throws Exception {
        SseEmitter emitter = new SseEmitter();
        when(notifyService.subscribe(anyString(), anyString())).thenReturn(emitter);

        mockMvc.perform(get("/api/notifications/subscribe")
                        .with(user(userDTO))
                        .with(csrf())
                        .header("Last-Event-ID", ""))
                .andExpect(status().isOk());
//                .andExpect(content().contentType(MediaType.TEXT_EVENT_STREAM_VALUE));

        verify(notifyService).subscribe(eq("test@example.com"), eq(""));
    }

    @Test
    @WithMockUser
    void testGetAllNotifications() throws Exception {
        List<NotifyDto.Response> notifications = Arrays.asList(
                new NotifyDto.Response(1L, "User1", "Test content", "TEST", "/test", false, LocalDateTime.now()),
                new NotifyDto.Response(2L, "User2", "Another test", "TEST", "/test2", true, LocalDateTime.now())
        );

        when(notifyService.getAllNotifications(anyString(), anyInt(), anyInt())).thenReturn((NotifyDto.PageResponse) notifications);

        mockMvc.perform(get("/api/notifications/all")
                        .with(user(userDTO))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
//        $ : JSON 응답의 루트, [0] : 첫 번째 요소, id : id 필드의 값이 1인지 확인
//        ,value(1) : 1인지 확인

        verify(notifyService).getAllNotifications(eq("test@example.com"), eq(0), eq(10));
    }

    @Test
    @WithMockUser
    void testMarkAsRead() throws Exception {
        doNothing().when(notifyService).markAsRead(anyString(), anyLong());

        mockMvc.perform(post("/api/notifications/{notificationId}/read", 1L)
                        .with(csrf())
                        .with(user(userDTO)))

                .andExpect(status().isOk());

        verify(notifyService).markAsRead(eq("test@example.com"), eq(1L));
    }}