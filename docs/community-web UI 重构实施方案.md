下面给你一版**基于当前 `D:\AppData\community-app\community-web` 真实结构**的 **UI 重构实施方案**。目标不是推翻重做，而是 **在现有 Vue3 + Vite + Arco Design Vue 基础上，做一轮“统一、精致、可持续”的前端视觉升级**。

---

# community-web UI 重构实施方案

## 一、重构目标

本轮 UI 重构目标：

1. **统一视觉系统**
   - 统一颜色、圆角、阴影、间距、标题层级
2. **提升首页与主框架颜值**
   - 让项目从“功能型页面”升级为“社区产品感页面”
3. **优化内容型页面体验**
   - 首页、帖子卡片、帖子详情、个人中心更有层次
4. **补强移动端体验**
   - 不只是响应式，而是更接近真正移动端产品
5. **为后续功能扩展建立可复用 UI 基座**
   - 后面继续做通知、聊天、运营页时不再样式散乱

---

## 二、重构范围

### 1）本轮纳入的核心文件
- `community-web/src/styles/index.scss`
- `community-web/src/layouts/DefaultLayout.vue`
- `community-web/src/components/post/PostCard.vue`
- `community-web/src/views/home/HomeView.vue`
- `community-web/src/views/auth/LoginView.vue`
- `community-web/src/views/auth/RegisterView.vue`
- `community-web/src/views/user/UserCenterView.vue`
- `community-web/src/views/user/UserProfileView.vue`
- `community-web/src/views/post/PostDetailView.vue`
- `community-web/src/views/post/PostPublishView.vue`

### 2）本轮暂不优先大改
- `src/views/chat/ChatView.vue`
- `src/views/notification/NotificationCenterView.vue`
- `src/views/history/HistoryListView.vue`
- `src/views/draft/DraftListView.vue`

这些页面先跟随全局样式收益，第二轮再专项精修。

---

## 三、当前问题归类

### A. 全局层
- 页面容器、圆角、阴影、间距未完全统一
- 缺少明确的设计 token
- 公共卡片/hero/section-head 没抽象

### B. 框架层
- `DefaultLayout.vue` 顶部导航偏功能化，产品感不足
- 移动端导航未形成独立方案

### C. 页面层
- 首页缺少“社区首页”的内容运营感
- 登录注册页过于朴素
- 个人中心、用户主页、发帖页功能强但视觉还不够精致

### D. 组件层
- `PostCard.vue` 还可继续增强层级与识别度
- 操作按钮、统计区、空状态可更统一

---

## 四、实施原则

### 原则 1：只做增量重构，不推倒重来
保留现有：
- 路由结构
- API 层
- Pinia 状态
- 业务交互

重点改：
- 样式结构
- 页面布局
- 公共视觉语言

### 原则 2：先全局、后页面
顺序必须是：

1. 全局 token
2. 布局壳
3. 公共组件
4. 重点页面
5. 移动端细化

### 原则 3：优先改用户最常看到的区域
优先级：
- 顶部导航
- 首页
- 帖子卡片
- 登录页
- 个人中心

---

## 五、实施分期

---

## Phase 1：设计系统收口

### 目标
建立统一的视觉基础，避免每个页面各写一套。

### 主要改动
文件：
- `community-web/src/styles/index.scss`

### 要做的内容
1. 补全全局 token
   - 颜色
   - 圆角
   - 阴影
   - 边框
   - 标题字号
   - 页面最大宽度
   - 间距体系

2. 抽公共样式类
   - `.app-page`
   - `.app-hero`
   - `.app-panel`
   - `.app-section-head`
   - `.app-stat-card`
   - `.app-chip`
   - `.app-toolbar`

3. 统一 Arco 二次样式
   - Button
   - Input
   - Select
   - Tabs
   - Pagination
   - Card

### 交付结果
- 全局样式变量统一
- 新旧页面都能直接复用公共样式

---

## Phase 2：主框架布局升级

### 目标
让整个站点先“看起来像一个完整产品”。

### 主要改动
文件：
- `community-web/src/layouts/DefaultLayout.vue`

### 要做的内容
1. 顶部导航重构
   - 品牌区更突出
   - 导航分组更清晰
   - 聊天/通知入口图标化
   - 用户区改成头像 + 菜单

2. 主体容器优化
   - 统一内容宽度
   - 页面留白更稳定
   - 头部 sticky 视觉更轻盈

3. 页脚重构
   - 从“阶段说明”改成更产品化的信息区

4. 移动端导航雏形
   - 小屏菜单折叠
   - 预留底部导航方案

### 交付结果
- 整站框架层统一升级
- 进入任意页面都能明显感觉更成熟

---

## Phase 3：首页内容化重构

### 目标
把首页从“帖子列表页”升级为“社区首页”。

### 主要改动
文件：
- `community-web/src/views/home/HomeView.vue`
- `community-web/src/components/post/PostCard.vue`

### 要做的内容

#### HomeView
1. 首页改双栏
   - 左：帖子流
   - 右：侧边栏

2. 侧边栏建议模块
   - 快速发帖
   - 热门标签
   - 社区说明
   - 推荐作者/活跃用户
   - 当前筛选摘要

3. Hero 精简并增强产品文案
   - 少一点技术说明
   - 多一点社区导向

4. 筛选区更轻量
   - 搜索 + 排序更紧凑
   - 可考虑吸顶

#### PostCard
1. 统一封面图比例
2. 强化标题视觉
3. 作者信息更清晰
4. 数据区 icon 化
5. tag 样式统一
6. hover 更高级
7. 状态标签更明确（公开/下架）

### 交付结果
- 首页更像内容社区首页
- 帖子列表更有产品感和浏览欲望

---

## Phase 4：登录注册页品牌化

### 目标
快速提升“第一眼颜值”。

### 主要改动
文件：
- `community-web/src/views/auth/LoginView.vue`
- `community-web/src/views/auth/RegisterView.vue`

### 要做的内容
1. 双栏布局
   - 左侧品牌介绍/欢迎语
   - 右侧表单卡片

2. 加背景装饰层
   - 渐变
   - 模糊光斑
   - 简单图形

3. 表单卡片升级
   - 更好看的标题、副标题、辅助文案
   - 登录/注册切换更自然

4. 移动端单栏适配
   - 保持主视觉仍然完整

### 交付结果
- 登录注册页不再像管理后台表单页
- 产品气质明显提升

---

## Phase 5：个人中心 / 用户主页升级

### 目标
让“我”和“TA”的页面更像真实社区个人空间。

### 主要改动
文件：
- `community-web/src/views/user/UserCenterView.vue`
- `community-web/src/views/user/UserProfileView.vue`

### 要做的内容

#### UserCenterView
1. Hero 区重做为“个人控制台”
2. 统计卡风格统一
3. Tabs 区和内容区分层
4. 帖子/收藏/点赞/关注模块统一 panel 结构
5. 资料编辑区更精致

#### UserProfileView
1. 强化用户名片感
2. 关注/私信按钮层级更明确
3. 个人简介与帖子列表关系更自然
4. 公开帖子区更像“个人内容流”

### 交付结果
- 用户中心更像产品核心页面
- 用户主页更适合展示社交关系和内容

---

## Phase 6：发帖页 / 帖子详情页精修

### 目标
提升内容生产与阅读体验。

### 主要改动
文件：
- `community-web/src/views/post/PostPublishView.vue`
- `community-web/src/views/post/PostDetailView.vue`

### 要做的内容

#### PostPublishView
1. 编辑区 / 预览区视觉分层增强
2. 工具栏优化
3. 上传区、附件区卡片化
4. 保存状态更醒目
5. 桌面端预览区 sticky
6. 移动端编辑体验优化

#### PostDetailView
1. 阅读头图和标题区更有张力
2. 作者信息块更独立
3. 互动按钮更自然
4. Markdown 内容排版升级
5. 评论区分层更清晰

### 交付结果
- 发帖页更像创作工作台
- 详情页更像真正阅读页

---

## Phase 7：移动端专项优化

### 目标
从“响应式能用”升级为“移动端体验顺手”。

### 主要改动
涉及：
- `DefaultLayout.vue`
- `HomeView.vue`
- `PostCard.vue`
- `UserCenterView.vue`
- `PostDetailView.vue`
- `PostPublishView.vue`

### 要做的内容
1. 顶部导航精简
2. 底部 TabBar 方案
3. 主操作按钮突出
4. 卡片内边距分档
5. 标题字号重排
6. 页内按钮更适合触控

### 交付结果
- 手机端更顺手
- 信息密度更合理

---

## 六、建议的实际执行顺序

### 第一周/第一轮
1. `src/styles/index.scss`
2. `src/layouts/DefaultLayout.vue`
3. `src/components/post/PostCard.vue`
4. `src/views/home/HomeView.vue`

### 第二周/第二轮
5. `src/views/auth/LoginView.vue`
6. `src/views/auth/RegisterView.vue`
7. `src/views/user/UserCenterView.vue`
8. `src/views/user/UserProfileView.vue`

### 第三周/第三轮
9. `src/views/post/PostPublishView.vue`
10. `src/views/post/PostDetailView.vue`
11. 移动端专项优化
12. 空状态 / 加载态 / 动效统一

---

## 七、验收标准

本轮 UI 重构完成后，至少满足：

### 视觉统一
- 圆角、阴影、标题层级明显统一
- 页面切换不再有“像不同项目”的感觉

### 首页提升
- 首页不再只是列表，而有社区内容首页感

### 核心页面提升
- 登录页、个人中心、帖子详情、发帖页明显更精致

### 移动端可用性提升
- 手机端导航和操作更顺手

### 工程可持续
- 后续新增页面能直接复用统一样式体系

---

## 八、建议产出物

我建议这次不只改代码，还同步沉淀 2 个文档：

1. `community-web/docs/ui-refactor-plan.md`
   - 本次实施方案
2. `community-web/docs/ui-design-tokens.md`
   - 颜色、圆角、阴影、间距、标题层级规范

这样后面继续迭代 UI 不会乱。

---

## 九、我建议你下一步直接这样做

如果你要我继续落地，最合理的顺序是：

### 下一步直接开始：
**第一阶段：全局设计系统 + 主框架布局升级**

也就是我先帮你实际改：

- `community-web/src/styles/index.scss`
- `community-web/src/layouts/DefaultLayout.vue`

这是最划算的一步，改完后全站观感会先整体抬一截。

如果你愿意，我下一条就可以继续：
**开始实际输出第一阶段改造方案细化 + 直接改代码。**

