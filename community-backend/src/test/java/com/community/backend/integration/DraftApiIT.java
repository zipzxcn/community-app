package com.community.backend.integration;

import com.community.backend.entity.FileObject;
import com.community.backend.entity.Tag;
import com.community.backend.mapper.FileObjectMapper;
import com.community.backend.mapper.TagMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Draft 模块集成测试：
 * 新建、更新、详情、发布与删除。
 */
class DraftApiIT extends SupportApiTest {

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private FileObjectMapper fileObjectMapper;

    @Test
    void shouldManageDraftLifecycle() throws Exception {
        Session session = createAndLogin("it_draft_user");
        Tag tag = Tag.builder()
                .name("draft-tag-" + System.nanoTime())
                .status("ACTIVE")
                .postCount(0)
                .build();
        tagMapper.insert(tag);
        FileObject file = FileObject.builder()
                .uploaderId(session.userId())
                .storageProvider("MINIO")
                .bucketName("community")
                .objectKey("user/" + session.userId() + "/POST/20260426/draft-attachment.png")
                .accessUrl("/api/v1/files/public/999999")
                .originalName("draft-attachment.png")
                .ext("png")
                .mimeType("image/png")
                .sizeBytes(2048L)
                .sortOrder(0)
                .status("UPLOADED")
                .build();
        fileObjectMapper.insert(file);

        JsonNode create = postJson("/api/v1/drafts",
                Map.of(
                        "title", "我的草稿",
                        "contentMd", "草稿正文 v1",
                        "coverUrl", "https://example.com/draft-cover.png",
                        "tagIds", new Long[]{tag.getId()},
                        "attachmentFileIds", new Long[]{file.getId()}
                ),
                session.accessToken());
        assertOk(create);
        Long draftId = create.path("data").path("draftId").asLong();
        Assertions.assertTrue(draftId > 0);

        JsonNode detail = getJson("/api/v1/drafts/" + draftId, session.accessToken());
        assertOk(detail);
        Assertions.assertEquals("我的草稿", detail.path("data").path("title").asText());
        Assertions.assertEquals(tag.getId(), detail.path("data").path("tagIds").get(0).asLong());
        Assertions.assertEquals(file.getId(), detail.path("data").path("attachmentFileIds").get(0).asLong());

        JsonNode update = putJson("/api/v1/drafts/" + draftId,
                Map.of(
                        "title", "我的草稿-更新",
                        "contentMd", "草稿正文 v2",
                        "coverUrl", "https://example.com/draft-cover-v2.png",
                        "tagIds", new Long[]{tag.getId()},
                        "attachmentFileIds", new Long[]{file.getId()},
                        "autoSave", true
                ),
                session.accessToken());
        assertOk(update);
        Assertions.assertEquals("我的草稿-更新", update.path("data").path("title").asText());
        Assertions.assertFalse(update.path("data").path("autoSavedAt").asText().isBlank());
        Assertions.assertEquals(tag.getId(), update.path("data").path("tags").get(0).path("id").asLong());
        Assertions.assertEquals(file.getId(), update.path("data").path("attachmentFiles").get(0).path("id").asLong());

        JsonNode list = getJson("/api/v1/drafts?page=1&size=20", session.accessToken());
        assertOk(list);
        Assertions.assertTrue(list.path("data").path("list").isArray());
        Assertions.assertTrue(list.path("data").path("list").size() >= 1);

        JsonNode publish = postJson("/api/v1/drafts/" + draftId + "/publish", Map.of(), session.accessToken());
        assertOk(publish);
        Long postId = publish.path("data").path("postId").asLong();
        Assertions.assertTrue(postId > 0);
        JsonNode postDetail = getJson("/api/v1/posts/" + postId, null);
        assertOk(postDetail);
        Assertions.assertEquals(tag.getId(), postDetail.path("data").path("tags").get(0).path("id").asLong());
        Assertions.assertEquals(file.getId(), postDetail.path("data").path("attachmentFileIds").get(0).asLong());

        JsonNode createToDelete = postJson("/api/v1/drafts",
                Map.of(
                        "title", "待删除草稿",
                        "contentMd", "待删除正文",
                        "coverUrl", "https://example.com/to-delete.png",
                        "tagIds", new Long[]{tag.getId()},
                        "attachmentFileIds", new Long[]{file.getId()}
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
