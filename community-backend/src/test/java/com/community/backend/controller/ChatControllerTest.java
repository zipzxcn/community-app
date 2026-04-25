package com.community.backend.controller;

import com.community.backend.common.api.PageResponse;
import com.community.backend.security.JwtAuthenticationFilter;
import com.community.backend.security.LoginUser;
import com.community.backend.service.ChatService;
import com.community.backend.vo.chat.ChatThreadVo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ChatController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldListThreads() throws Exception {
        mockLogin(1L);

        ChatThreadVo thread = new ChatThreadVo();
        thread.setThreadId(200L);
        thread.setStatus("ACTIVE");
        thread.setUnreadCount(2);

        PageResponse<ChatThreadVo> page = PageResponse.<ChatThreadVo>builder()
                .list(List.of(thread))
                .page(1L)
                .size(20L)
                .total(1L)
                .hasMore(false)
                .build();
        when(chatService.listThreads(eq(1L), eq(1L), eq(20L))).thenReturn(page);

        mockMvc.perform(get("/api/v1/chat/threads")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].threadId").value(200))
                .andExpect(jsonPath("$.data.list[0].status").value("ACTIVE"));
    }

    private void mockLogin(Long userId) {
        LoginUser loginUser = new LoginUser(userId, "tester", "", true, List.of("ROLE_USER"));
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                loginUser, null, loginUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
