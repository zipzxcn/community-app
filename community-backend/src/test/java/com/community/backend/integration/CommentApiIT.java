package com.community.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * Comment 模块集成测试：
 * 评论、回复、点赞/取消点赞、删除。
 */
class CommentApiIT extends SupportApiTest {

    @Test
    void shouldCreateReplyLikeAndDeleteComment() throws Exception {
        Session author = createAndLogin("it_comment_author");
        Session commenter = createAndLogin("it_comment_user");

        Long postId = createPost(author.accessToken(), "评论测试帖", "评论测试正文");
        Long commentId = createComment(commenter.accessToken(), postId, "这是一条评论");

        JsonNode reply = postJson("/api/v1/comments/" + commentId + "/reply",
                Map.of("parentId", commentId, "content", "这是作者回复"),
                author.accessToken());
        assertOk(reply);
        Long replyId = reply.path("data").path("id").asLong();
        Assertions.assertTrue(replyId > 0);

        JsonNode like = postJson("/api/v1/comments/" + commentId + "/like", Map.of(), author.accessToken());
        assertOk(like);
        JsonNode unlike = deleteJson("/api/v1/comments/" + commentId + "/like", author.accessToken());
        assertOk(unlike);

        JsonNode delete = deleteJson("/api/v1/comments/" + commentId, commenter.accessToken());
        assertOk(delete);

        JsonNode list = getJson("/api/v1/posts/" + postId + "/comments?page=1&size=20", author.accessToken());
        assertOk(list);
        // 被删除的根评论不应再出现在 NORMAL 列表中
        boolean exists = false;
        for (JsonNode item : list.path("data").path("list")) {
            if (item.path("id").asLong() == commentId) {
                exists = true;
                break;
            }
        }
        Assertions.assertFalse(exists);
    }
}

