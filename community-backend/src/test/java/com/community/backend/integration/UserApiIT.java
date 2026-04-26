package com.community.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * User 模块集成测试：
 * 用户中心帖子/收藏/点赞列表与权限边界。
 */
class UserApiIT extends SupportApiTest {

    @Test
    void shouldListUserCenterPostsFavoritesAndLikes() throws Exception {
        Session author = createAndLogin("it_user_center_author");
        Session owner = createAndLogin("it_user_center_owner");

        Long postId = createPost(author.accessToken(), "用户中心帖子", "用于用户中心列表回归");
        assertOk(postJson("/api/v1/posts/" + postId + "/favorite", Map.of(), owner.accessToken()));
        assertOk(postJson("/api/v1/posts/" + postId + "/like", Map.of(), owner.accessToken()));

        JsonNode posts = getJson("/api/v1/users/" + author.userId() + "/posts?page=1&size=20", null);
        assertOk(posts);
        Assertions.assertTrue(containsPost(posts.path("data").path("list"), postId));

        JsonNode favorites = getJson("/api/v1/users/" + owner.userId() + "/favorites?page=1&size=20", owner.accessToken());
        assertOk(favorites);
        Assertions.assertTrue(containsPost(favorites.path("data").path("list"), postId));

        JsonNode likes = getJson("/api/v1/users/" + owner.userId() + "/likes?page=1&size=20", owner.accessToken());
        assertOk(likes);
        Assertions.assertTrue(containsPost(likes.path("data").path("list"), postId));

        JsonNode forbidden = getJson("/api/v1/users/" + owner.userId() + "/favorites?page=1&size=20", author.accessToken());
        Assertions.assertEquals(20003, forbidden.path("code").asInt());

        JsonNode unauthorized = getJson("/api/v1/users/" + owner.userId() + "/likes?page=1&size=20", null);
        Assertions.assertEquals(20001, unauthorized.path("code").asInt());
    }

    @Test
    void shouldExposeHiddenPostsOnlyToOwner() throws Exception {
        Session author = createAndLogin("it_user_hidden_author");
        Long postId = createPost(author.accessToken(), "隐藏帖子", "用于隐藏列表回归");

        JsonNode hide = patchJson("/api/v1/posts/" + postId + "/hide", Map.of("hidden", true), author.accessToken());
        assertOk(hide);

        JsonNode ownerPosts = getJson("/api/v1/users/" + author.userId() + "/posts?page=1&size=20", author.accessToken());
        assertOk(ownerPosts);
        Assertions.assertTrue(containsPost(ownerPosts.path("data").path("list"), postId));
        Assertions.assertEquals("HIDDEN", ownerPosts.path("data").path("list").get(0).path("status").asText());

        JsonNode guestPosts = getJson("/api/v1/users/" + author.userId() + "/posts?page=1&size=20", null);
        assertOk(guestPosts);
        Assertions.assertFalse(containsPost(guestPosts.path("data").path("list"), postId));
    }

    /**
     * 判断分页列表中是否包含目标 postId。
     */
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
