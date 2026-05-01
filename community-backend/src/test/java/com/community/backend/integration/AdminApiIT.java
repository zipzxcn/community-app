package com.community.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

class AdminApiIT extends SupportApiTest {

    @Test
    void shouldModeratePostsFromAdminEndpoints() throws Exception {
        Session operator = createAndLogin("it_admin_operator");
        String title = "后台审核帖子_" + UUID.randomUUID().toString().substring(0, 8);
        Long postId = createPost(operator.accessToken(), title, "用于后台下架删帖回归");

        JsonNode overview = getJson("/api/v1/admin/overview", operator.accessToken());
        assertOk(overview);

        JsonNode posts = getJson("/api/v1/admin/posts?page=1&size=10&keyword=" + title, operator.accessToken());
        assertOk(posts);
        Assertions.assertTrue(containsPost(posts.path("data").path("list"), postId));

        JsonNode hide = patchJson("/api/v1/admin/posts/" + postId + "/hide", Map.of("hidden", true), operator.accessToken());
        assertOk(hide);

        JsonNode hiddenPosts = getJson("/api/v1/admin/posts?page=1&size=10&status=HIDDEN&keyword=" + title, operator.accessToken());
        assertOk(hiddenPosts);
        Assertions.assertTrue(containsPost(hiddenPosts.path("data").path("list"), postId));
        Assertions.assertEquals("HIDDEN", hiddenPosts.path("data").path("list").get(0).path("status").asText());

        JsonNode delete = deleteJson("/api/v1/admin/posts/" + postId, operator.accessToken());
        assertOk(delete);

        JsonNode afterDelete = getJson("/api/v1/admin/posts?page=1&size=10&keyword=" + title, operator.accessToken());
        assertOk(afterDelete);
        Assertions.assertFalse(containsPost(afterDelete.path("data").path("list"), postId));
    }

    private boolean containsPost(JsonNode listNode, Long postId) {
        if (listNode == null || !listNode.isArray()) {
            return false;
        }
        for (JsonNode item : listNode) {
            if (item.path("id").asLong() == postId) {
                return true;
            }
        }
        return false;
    }
}
