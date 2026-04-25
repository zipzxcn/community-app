package com.community.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * History 模块集成测试：
 * 记录、列表、删除与清空。
 */
class HistoryApiIT extends SupportApiTest {

    @Test
    void shouldManageBrowseHistories() throws Exception {
        Session author = createAndLogin("it_history_author");
        Session viewer = createAndLogin("it_history_viewer");
        Long postId = createPost(author.accessToken(), "历史测试帖子", "历史测试正文");

        JsonNode record1 = postJson("/api/v1/histories/" + postId, Map.of(), viewer.accessToken());
        assertOk(record1);
        JsonNode record2 = postJson("/api/v1/histories/" + postId, Map.of(), viewer.accessToken());
        assertOk(record2);

        JsonNode list = getJson("/api/v1/histories?page=1&size=20", viewer.accessToken());
        assertOk(list);
        JsonNode items = list.path("data").path("list");
        Assertions.assertTrue(items.isArray());
        Assertions.assertTrue(items.size() >= 1);

        long historyId = 0L;
        int viewCount = 0;
        for (JsonNode item : items) {
            if (item.path("postId").asLong() == postId) {
                historyId = item.path("id").asLong();
                viewCount = item.path("viewCount").asInt();
                break;
            }
        }
        Assertions.assertTrue(historyId > 0);
        Assertions.assertTrue(viewCount >= 2);

        JsonNode delete = deleteJson("/api/v1/histories/" + historyId, viewer.accessToken());
        assertOk(delete);

        JsonNode listAfterDelete = getJson("/api/v1/histories?page=1&size=20", viewer.accessToken());
        assertOk(listAfterDelete);
        boolean containsDeleted = false;
        for (JsonNode item : listAfterDelete.path("data").path("list")) {
            if (item.path("id").asLong() == historyId) {
                containsDeleted = true;
                break;
            }
        }
        Assertions.assertFalse(containsDeleted);

        JsonNode recordAgain = postJson("/api/v1/histories/" + postId, Map.of(), viewer.accessToken());
        assertOk(recordAgain);
        JsonNode clear = deleteJson("/api/v1/histories", viewer.accessToken());
        assertOk(clear);

        JsonNode listAfterClear = getJson("/api/v1/histories?page=1&size=20", viewer.accessToken());
        assertOk(listAfterClear);
        Assertions.assertEquals(0, listAfterClear.path("data").path("list").size());
    }
}
