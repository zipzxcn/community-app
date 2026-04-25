package com.community.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 集成测试公共基类：
 * 1) 使用真实 Spring 上下文 + MockMvc
 * 2) 走真实数据库/配置（dev）
 * 3) 提供注册、登录、发帖等常用辅助方法
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public abstract class SupportApiTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected record Session(String username, String password, Long userId, String accessToken, String refreshToken) {
    }

    /**
     * 创建唯一账号并登录，返回会话信息。
     */
    protected Session createAndLogin(String prefix) throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String username = prefix + "_" + suffix;
        String password = "12345678";
        String nickname = "IT_" + suffix;

        JsonNode register = postJson("/api/v1/auth/register",
                Map.of("username", username, "password", password, "nickname", nickname),
                null);
        assertOk(register);
        Long userId = register.path("data").path("userId").asLong();
        Assertions.assertTrue(userId > 0);

        JsonNode login = postJson("/api/v1/auth/login",
                Map.of("username", username, "password", password),
                null);
        assertOk(login);

        String accessToken = login.path("data").path("accessToken").asText();
        String refreshToken = login.path("data").path("refreshToken").asText();
        Assertions.assertFalse(accessToken.isBlank());
        Assertions.assertFalse(refreshToken.isBlank());

        return new Session(username, password, userId, accessToken, refreshToken);
    }

    /**
     * 以当前 token 发布帖子并返回 postId。
     */
    protected Long createPost(String accessToken, String title, String contentMd) throws Exception {
        JsonNode create = postJson("/api/v1/posts",
                Map.of(
                        "title", title,
                        "contentMd", contentMd,
                        "coverUrl", "https://example.com/cover.png",
                        "allowComment", true,
                        "tagIds", new Long[]{},
                        "attachmentFileIds", new Long[]{}
                ),
                accessToken);
        assertOk(create);
        Long postId = create.path("data").path("postId").asLong();
        Assertions.assertTrue(postId > 0);
        return postId;
    }

    /**
     * 发表评论并返回 commentId。
     */
    protected Long createComment(String accessToken, Long postId, String content) throws Exception {
        JsonNode comment = postJson("/api/v1/posts/" + postId + "/comments",
                Map.of("content", content),
                accessToken);
        assertOk(comment);
        Long commentId = comment.path("data").path("id").asLong();
        Assertions.assertTrue(commentId > 0);
        return commentId;
    }

    /**
     * 发送 POST JSON 请求。
     */
    protected JsonNode postJson(String path, Object body, String accessToken) throws Exception {
        var builder = post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
        if (accessToken != null) {
            builder.header("Authorization", "Bearer " + accessToken);
        }
        MvcResult result = mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andReturn();
        return readJson(result);
    }

    /**
     * 发送 PATCH JSON 请求。
     */
    protected JsonNode patchJson(String path, Object body, String accessToken) throws Exception {
        var builder = patch(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
        if (accessToken != null) {
            builder.header("Authorization", "Bearer " + accessToken);
        }
        MvcResult result = mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andReturn();
        return readJson(result);
    }

    /**
     * 发送 GET 请求。
     */
    protected JsonNode getJson(String path, String accessToken) throws Exception {
        var builder = get(path);
        if (accessToken != null) {
            builder.header("Authorization", "Bearer " + accessToken);
        }
        MvcResult result = mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andReturn();
        return readJson(result);
    }

    /**
     * 发送 DELETE 请求。
     */
    protected JsonNode deleteJson(String path, String accessToken) throws Exception {
        var builder = delete(path);
        if (accessToken != null) {
            builder.header("Authorization", "Bearer " + accessToken);
        }
        MvcResult result = mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andReturn();
        return readJson(result);
    }

    /**
     * 发送 PUT JSON 请求。
     */
    protected JsonNode putJson(String path, Object body, String accessToken) throws Exception {
        var builder = put(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
        if (accessToken != null) {
            builder.header("Authorization", "Bearer " + accessToken);
        }
        MvcResult result = mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andReturn();
        return readJson(result);
    }

    /**
     * 读取响应 JSON。
     */
    protected JsonNode readJson(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(body);
    }

    /**
     * 断言接口返回成功。
     */
    protected void assertOk(JsonNode json) {
        Assertions.assertEquals(0, json.path("code").asInt(), "响应非成功: " + json);
    }
}

