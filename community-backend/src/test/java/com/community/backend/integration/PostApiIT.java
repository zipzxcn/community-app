package com.community.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Post 模块集成测试：
 * 发帖、详情、点赞收藏及取消。
 */
class PostApiIT extends SupportApiTest {

    @Test
    void shouldCreateAndInteractPost() throws Exception {
        Session author = createAndLogin("it_post_author");
        Session viewer = createAndLogin("it_post_viewer");

        Long postId = createPost(author.accessToken(), "IT 帖子", "IT 帖子正文");

        JsonNode detailAsGuest = getJson("/api/v1/posts/" + postId, null);
        assertOk(detailAsGuest);
        Assertions.assertEquals(postId.longValue(), detailAsGuest.path("data").path("id").asLong());

        JsonNode like = postJson("/api/v1/posts/" + postId + "/like", java.util.Map.of(), viewer.accessToken());
        assertOk(like);
        JsonNode favorite = postJson("/api/v1/posts/" + postId + "/favorite", java.util.Map.of(), viewer.accessToken());
        assertOk(favorite);

        JsonNode detailWithViewer = getJson("/api/v1/posts/" + postId, viewer.accessToken());
        assertOk(detailWithViewer);
        Assertions.assertTrue(detailWithViewer.path("data").path("liked").asBoolean());
        Assertions.assertTrue(detailWithViewer.path("data").path("favorited").asBoolean());

        JsonNode unlike = deleteJson("/api/v1/posts/" + postId + "/like", viewer.accessToken());
        assertOk(unlike);
        JsonNode unfavorite = deleteJson("/api/v1/posts/" + postId + "/favorite", viewer.accessToken());
        assertOk(unfavorite);

        JsonNode detailAfterCancel = getJson("/api/v1/posts/" + postId, viewer.accessToken());
        assertOk(detailAfterCancel);
        Assertions.assertFalse(detailAfterCancel.path("data").path("liked").asBoolean());
        Assertions.assertFalse(detailAfterCancel.path("data").path("favorited").asBoolean());
    }
}

