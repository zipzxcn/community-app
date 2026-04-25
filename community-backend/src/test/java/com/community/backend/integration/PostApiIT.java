package com.community.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Post 模块集成测试：
 * 发帖、编辑、显隐、删除、点赞收藏及取消。
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

        JsonNode update = putJson("/api/v1/posts/" + postId,
                java.util.Map.of(
                        "title", "IT 帖子-更新",
                        "contentMd", "更新后的正文",
                        "coverUrl", "https://example.com/cover-updated.png",
                        "allowComment", true,
                        "tagIds", new Long[]{},
                        "attachmentFileIds", new Long[]{}
                ),
                author.accessToken());
        assertOk(update);

        JsonNode detailAfterUpdate = getJson("/api/v1/posts/" + postId, null);
        assertOk(detailAfterUpdate);
        Assertions.assertEquals("IT 帖子-更新", detailAfterUpdate.path("data").path("title").asText());

        JsonNode hide = patchJson("/api/v1/posts/" + postId + "/hide",
                java.util.Map.of("hidden", true),
                author.accessToken());
        assertOk(hide);

        JsonNode detailAfterHide = getJson("/api/v1/posts/" + postId, null);
        Assertions.assertEquals(23001, detailAfterHide.path("code").asInt());

        JsonNode show = patchJson("/api/v1/posts/" + postId + "/hide",
                java.util.Map.of("hidden", false),
                author.accessToken());
        assertOk(show);

        JsonNode detailAfterShow = getJson("/api/v1/posts/" + postId, null);
        assertOk(detailAfterShow);
        Assertions.assertEquals(postId.longValue(), detailAfterShow.path("data").path("id").asLong());

        JsonNode tags = getJson("/api/v1/posts/tags", null);
        assertOk(tags);
        Assertions.assertTrue(tags.path("data").isArray());

        JsonNode delete = deleteJson("/api/v1/posts/" + postId, author.accessToken());
        assertOk(delete);

        JsonNode detailAfterDelete = getJson("/api/v1/posts/" + postId, null);
        Assertions.assertEquals(23001, detailAfterDelete.path("code").asInt());
    }
}
