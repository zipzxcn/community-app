package com.community.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * Follow 模块集成测试：
 * 关注、粉丝、互关列表主链路。
 */
class FollowApiIT extends SupportApiTest {

    @Test
    void shouldListMutualCorrectly() throws Exception {
        Session me = createAndLogin("it_follow_me");
        Session mutualUser = createAndLogin("it_follow_mutual");
        Session oneWayUser = createAndLogin("it_follow_one_way");

        assertOk(postJson("/api/v1/follows/" + mutualUser.userId(), Map.of(), me.accessToken()));
        assertOk(postJson("/api/v1/follows/" + oneWayUser.userId(), Map.of(), me.accessToken()));
        assertOk(postJson("/api/v1/follows/" + me.userId(), Map.of(), mutualUser.accessToken()));

        JsonNode following = getJson("/api/v1/follows/following?page=1&size=20", me.accessToken());
        assertOk(following);
        Assertions.assertTrue(containsUser(following.path("data").path("list"), mutualUser.userId()));
        Assertions.assertTrue(containsUser(following.path("data").path("list"), oneWayUser.userId()));

        JsonNode mutual = getJson("/api/v1/follows/mutual?page=1&size=20", me.accessToken());
        assertOk(mutual);
        Assertions.assertTrue(containsUser(mutual.path("data").path("list"), mutualUser.userId()));
        Assertions.assertFalse(containsUser(mutual.path("data").path("list"), oneWayUser.userId()));
    }

    /**
     * 判断列表中是否包含目标用户。
     */
    private boolean containsUser(JsonNode listNode, Long userId) {
        if (listNode == null || !listNode.isArray()) {
            return false;
        }
        for (JsonNode item : listNode) {
            if (item.path("id").asLong() == userId) {
                return true;
            }
        }
        return false;
    }
}
