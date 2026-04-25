package com.community.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * Notification 模块集成测试：
 * 关注/评论/点赞触发通知，查询未读并执行已读动作。
 */
class NotificationApiIT extends SupportApiTest {

    @Test
    void shouldTriggerAndReadNotifications() throws Exception {
        Session receiver = createAndLogin("it_notice_receiver");
        Session actor = createAndLogin("it_notice_actor");

        Long postId = createPost(receiver.accessToken(), "通知测试帖", "通知测试正文");

        JsonNode follow = postJson("/api/v1/follows/" + receiver.userId(), Map.of(), actor.accessToken());
        assertOk(follow);
        JsonNode comment = postJson("/api/v1/posts/" + postId + "/comments", Map.of("content", "通知评论"), actor.accessToken());
        assertOk(comment);
        JsonNode like = postJson("/api/v1/posts/" + postId + "/like", Map.of(), actor.accessToken());
        assertOk(like);

        JsonNode unread = getJson("/api/v1/notifications/unread-count", receiver.accessToken());
        assertOk(unread);
        Assertions.assertTrue(unread.path("data").path("total").asInt() >= 3);
        Assertions.assertTrue(unread.path("data").path("followCount").asInt() >= 1);
        Assertions.assertTrue(unread.path("data").path("commentCount").asInt() >= 1);
        Assertions.assertTrue(unread.path("data").path("postLikeCount").asInt() >= 1);

        JsonNode list = getJson("/api/v1/notifications?page=1&size=20", receiver.accessToken());
        assertOk(list);
        Assertions.assertTrue(list.path("data").path("list").isArray());
        Assertions.assertTrue(list.path("data").path("list").size() >= 1);
        Long firstId = list.path("data").path("list").get(0).path("id").asLong();
        Assertions.assertTrue(firstId > 0);

        JsonNode markOne = patchJson("/api/v1/notifications/" + firstId + "/read", Map.of(), receiver.accessToken());
        assertOk(markOne);

        JsonNode markAllComment = patchJson("/api/v1/notifications/read-all", Map.of("type", "COMMENT"), receiver.accessToken());
        assertOk(markAllComment);
    }
}

