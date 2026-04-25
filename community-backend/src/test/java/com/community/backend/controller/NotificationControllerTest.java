package com.community.backend.controller;

import com.community.backend.common.api.PageResponse;
import com.community.backend.security.JwtAuthenticationFilter;
import com.community.backend.security.LoginUser;
import com.community.backend.service.NotificationService;
import com.community.backend.vo.notification.NotificationItemVo;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldListNotifications() throws Exception {
        mockLogin(1L);

        NotificationItemVo item = new NotificationItemVo();
        item.setId(100L);
        item.setType("COMMENT");
        item.setTitle("你的帖子有新评论");

        PageResponse<NotificationItemVo> page = PageResponse.<NotificationItemVo>builder()
                .list(List.of(item))
                .page(1L)
                .size(20L)
                .total(1L)
                .hasMore(false)
                .build();
        when(notificationService.list(eq(1L), any(), any(), any(), any(), eq(1L), eq(20L))).thenReturn(page);

        mockMvc.perform(get("/api/v1/notifications")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].id").value(100))
                .andExpect(jsonPath("$.data.list[0].type").value("COMMENT"));
    }

    private void mockLogin(Long userId) {
        LoginUser loginUser = new LoginUser(userId, "tester", "", true, List.of("ROLE_USER"));
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                loginUser, null, loginUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
