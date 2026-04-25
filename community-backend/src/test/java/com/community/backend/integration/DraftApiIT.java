package com.community.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * Draft 模块集成测试：
 * 新建、更新、详情、发布与删除。
 */
class DraftApiIT extends SupportApiTest {

    @Test
    void shouldManageDraftLifecycle() throws Exception {
        Session session = createAndLogin("it_draft_user");

        JsonNode create = postJson("/api/v1/drafts",
                Map.of(
                        "title", "我的草稿",
                        "contentMd", "草稿正文 v1",
                        "coverUrl", "https://example.com/draft-cover.png"
                ),
                session.accessToken());
        assertOk(create);
        Long draftId = create.path("data").path("draftId").asLong();
        Assertions.assertTrue(draftId > 0);

        JsonNode detail = getJson("/api/v1/drafts/" + draftId, session.accessToken());
        assertOk(detail);
        Assertions.assertEquals("我的草稿", detail.path("data").path("title").asText());

        JsonNode update = putJson("/api/v1/drafts/" + draftId,
                Map.of(
                        "title", "我的草稿-更新",
                        "contentMd", "草稿正文 v2",
                        "coverUrl", "https://example.com/draft-cover-v2.png",
                        "autoSave", true
                ),
                session.accessToken());
        assertOk(update);
        Assertions.assertEquals("我的草稿-更新", update.path("data").path("title").asText());
        Assertions.assertFalse(update.path("data").path("autoSavedAt").asText().isBlank());

        JsonNode list = getJson("/api/v1/drafts?page=1&size=20", session.accessToken());
        assertOk(list);
        Assertions.assertTrue(list.path("data").path("list").isArray());
        Assertions.assertTrue(list.path("data").path("list").size() >= 1);

        JsonNode publish = postJson("/api/v1/drafts/" + draftId + "/publish", Map.of(), session.accessToken());
        assertOk(publish);
        Long postId = publish.path("data").path("postId").asLong();
        Assertions.assertTrue(postId > 0);

        JsonNode createToDelete = postJson("/api/v1/drafts",
                Map.of(
                        "title", "待删除草稿",
                        "contentMd", "待删除正文",
                        "coverUrl", "https://example.com/to-delete.png"
                ),
                session.accessToken());
        assertOk(createToDelete);
        Long draftIdToDelete = createToDelete.path("data").path("draftId").asLong();
        Assertions.assertTrue(draftIdToDelete > 0);

        JsonNode delete = deleteJson("/api/v1/drafts/" + draftIdToDelete, session.accessToken());
        assertOk(delete);

        JsonNode listAfterDelete = getJson("/api/v1/drafts?page=1&size=20", session.accessToken());
        assertOk(listAfterDelete);
        boolean containsDeleted = false;
        for (JsonNode item : listAfterDelete.path("data").path("list")) {
            if (item.path("id").asLong() == draftIdToDelete) {
                containsDeleted = true;
                break;
            }
        }
        Assertions.assertFalse(containsDeleted);
    }
}
