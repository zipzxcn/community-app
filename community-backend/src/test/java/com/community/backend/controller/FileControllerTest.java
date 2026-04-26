package com.community.backend.controller;

import com.community.backend.security.JwtAuthenticationFilter;
import com.community.backend.security.LoginUser;
import com.community.backend.service.FileService;
import com.community.backend.vo.file.FileDownloadVo;
import com.community.backend.vo.file.UploadTokenVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FileController.class)
@AutoConfigureMockMvc(addFilters = false)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FileService fileService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldGetUploadToken() throws Exception {
        mockLogin(1L);

        UploadTokenVo vo = new UploadTokenVo();
        vo.setProvider("MINIO");
        vo.setUploadUrl("http://localhost:9000/upload-url");
        vo.setBucketName("community-dev");
        vo.setObjectKey("user/1/POST/20260424/abc.png");
        vo.setHeaders(Map.of("Content-Type", "image/png"));
        when(fileService.createUploadToken(eq(1L), any())).thenReturn(vo);

        String body = objectMapper.writeValueAsString(Map.of(
                "bizType", "POST",
                "fileName", "a.png",
                "contentType", "image/png",
                "size", 123
        ));

        mockMvc.perform(post("/api/v1/files/upload-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.provider").value("MINIO"))
                .andExpect(jsonPath("$.data.bucketName").value("community-dev"));
    }

    @Test
    void shouldReadPublicFile() throws Exception {
        when(fileService.loadPublicFile(99L)).thenReturn(new FileDownloadVo(
                "image/png",
                4L,
                "avatar.png",
                new ByteArrayInputStream(new byte[]{1, 2, 3, 4})
        ));

        mockMvc.perform(get("/api/v1/files/public/99"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(new byte[]{1, 2, 3, 4}));
    }

    private void mockLogin(Long userId) {
        LoginUser loginUser = new LoginUser(userId, "tester", "", true, List.of("ROLE_USER"));
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                loginUser, null, loginUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
