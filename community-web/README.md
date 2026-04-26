# community-web

社区平台一期前端，基于 `Vue 3 + TypeScript + Vite + Pinia + Vue Router + Arco Design Vue`。

当前状态：`一期 MVP 主链路已打通`，已覆盖登录注册、帖子流、帖子详情、发布/草稿、评论互动、用户中心、通知中心、浏览历史、关注互关、私信聊天一期。

## 开发命令

```bash
npm install
npm run dev
npm run build
```

## 主要页面

- `/` 首页帖子流
- `/posts/publish` 发布 / 编辑帖子
- `/posts/:postId` 帖子详情
- `/drafts` 草稿箱
- `/histories` 浏览历史
- `/users/search` 用户搜索
- `/users/:userId` 用户主页
- `/me` 我的个人中心
- `/notifications` 通知中心
- `/chat` 私信聊天一期

## 一期聊天说明

- 历史消息：REST 拉取
- 发消息：REST 提交
- 新消息/已读回执：WebSocket 推送
- 实时通道异常时，前端仍保留手动刷新与定时兜底

## 当前建议

当前仓库更适合先进入“前端统一视觉和交互打磨”阶段，而不是继续扩新功能。建议下一步优先优化：

1. 全局导航、页头、留白和卡片体系
2. 首页 / 帖子详情 / 聊天页三大高频页面
3. 空状态、错误态、加载态、移动端适配
4. 演示截图、一期验收清单、回归测试记录
