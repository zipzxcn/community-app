ALTER TABLE post_draft
    ADD COLUMN tag_ids_json TEXT NULL COMMENT '草稿标签ID列表(JSON)' AFTER cover_url,
    ADD COLUMN attachment_file_ids_json TEXT NULL COMMENT '草稿附件文件ID列表(JSON)' AFTER tag_ids_json;
