# 前端 UI 优化清单

> 适用范围：`D:\AppData\community-app\community-web`
>
> 技术栈基线：`Vue 3 + TypeScript + Vite + Arco Design Vue`
>
> 目的：在不推翻现有业务结构的前提下，对当前社区前端做一轮“统一、精致、可持续”的 UI 升级，作为后续页面优化与代码落地的执行清单。

---

## 1. 当前前端页面现状

当前前端已经具备以下基础：

- 已有统一浅色背景与卡片化风格
- 已接入 `Arco Design Vue`
- 已完成首页、帖子详情、发帖、个人中心、用户主页、聊天、通知、草稿、历史等页面路由
- 已具备一定响应式处理
- 重点业务闭环已形成：登录注册、发帖、帖子流、详情、评论、收藏、点赞、关注、私信、通知、浏览历史

但目前 UI 层仍存在以下问题：

### 1.1 视觉系统未完全统一

- 圆角、阴影、留白、标题层级存在页面间差异
- Hero、Panel、Section Header、Stat Card 没有形成公共样式体系
- 组件状态色、弱文案色、边框色缺少统一 token

### 1.2 主框架产品感仍偏弱

- 顶部导航更偏“功能入口集合”，还不够“产品首页导航”
- 用户区、通知区、聊天入口还可更精致
- 页脚仍偏说明性质，产品感不强

### 1.3 首页内容运营感不足

- 首页更像“帖子列表页”，还不像“社区首页”
- 缺少侧边栏内容承载区
- 帖子卡片的信息层级还可以继续强化

### 1.4 重点页面完成了功能，但精致度不够

- 登录/注册页明显偏朴素
- 个人中心与用户主页视觉层级可以更强
- 发帖页仍更接近“表单页”，还不够像“创作工作台”
- 帖子详情页的阅读沉浸感仍可提升

### 1.5 移动端仅完成基础响应式

- 当前更多是“桌面改单栏”
- 还没有形成专门的移动端导航与触控体验设计

---

## 2. 本轮优化总体目标

本轮前端 UI 优化目标如下：

1. **统一设计语言**
2. **提升主框架与首页颜值**
3. **优化内容浏览、个人空间、创作发布三大核心场景**
4. **补强移动端体验**
5. **沉淀可复用的样式体系，避免后续继续散乱**

---

## 3. 优化范围

### 3.1 本轮重点文件

- `community-web/src/styles/index.scss`
- `community-web/src/layouts/DefaultLayout.vue`
- `community-web/src/components/post/PostCard.vue`
- `community-web/src/views/home/HomeView.vue`
- `community-web/src/views/auth/LoginView.vue`
- `community-web/src/views/auth/RegisterView.vue`
- `community-web/src/views/user/UserCenterView.vue`
- `community-web/src/views/user/UserProfileView.vue`
- `community-web/src/views/post/PostPublishView.vue`
- `community-web/src/views/post/PostDetailView.vue`

### 3.2 暂不作为第一优先级的页面

- `community-web/src/views/chat/ChatView.vue`
- `community-web/src/views/notification/NotificationCenterView.vue`
- `community-web/src/views/history/HistoryListView.vue`
- `community-web/src/views/draft/DraftListView.vue`

说明：

- 这些页面先跟随全局样式优化受益
- 第二轮再做专项精修

---

## 4. UI 优化实施原则

### 4.1 增量改造，不推翻重做

保留以下既有结构：

- 路由结构
- API 层
- Pinia 状态
- 页面业务交互
- Arco 组件体系

重点调整：

- 布局结构
- 公共样式
- 视觉层级
- 页面壳

### 4.2 先全局，后页面

严格按以下顺序推进：

1. 设计 token
2. 全局布局壳
3. 公共组件
4. 首页与登录页
5. 个人中心与内容页
6. 移动端专项

### 4.3 优先做用户最常看到的区域

优先级最高的区域：

- 顶部导航
- 首页
- 帖子卡片
- 登录页 / 注册页
- 个人中心

---

## 5. 优化清单（按优先级）

---

## P0：设计系统统一

### 任务 1：补齐全局设计 Token

**目标文件：**

- `community-web/src/styles/index.scss`

**要做内容：**

- 统一主色、辅色、成功/警告/危险色
- 统一文本色分层
- 统一边框色、卡片背景色、页面背景色
- 统一圆角等级
- 统一阴影等级
- 统一页面最大宽度
- 统一间距等级

**建议补充变量：**

- `--app-primary`
- `--app-primary-soft`
- `--app-text-1`
- `--app-text-2`
- `--app-text-3`
- `--app-border-color`
- `--app-bg-page`
- `--app-bg-card`
- `--app-radius-sm`
- `--app-radius-md`
- `--app-radius-lg`
- `--app-radius-xl`
- `--app-shadow-xs`
- `--app-shadow-sm`
- `--app-shadow-md`
- `--app-shadow-lg`
- `--app-page-width`

**完成标准：**

- 后续页面不再手写大量零散颜色和圆角值
- 新页面能直接复用全局变量

---

### 任务 2：抽取全局公共 UI 类

**目标文件：**

- `community-web/src/styles/index.scss`

**要做内容：**

建立公共类，减少每页重复造样式：

- `.app-page`
- `.app-hero`
- `.app-panel`
- `.app-section-head`
- `.app-stat-card`
- `.app-chip`
- `.app-toolbar`
- `.app-empty-state`

**完成标准：**

- 首页、个人中心、帖子详情等能直接共享公共壳样式
- 页面间视觉语言更一致

---

### 任务 3：统一 Arco 二次样式

**目标文件：**

- `community-web/src/styles/index.scss`

**要做内容：**

统一以下组件外观：

- `Button`
- `Input`
- `InputPassword`
- `Select`
- `Tabs`
- `Pagination`
- `Card`
- `Tag`
- `Avatar`

**完成标准：**

- Arco 基础控件风格统一
- 按钮、表单、分页、标签不再像独立拼接出来的页面

---

## P1：主框架与首页升级

### 任务 4：重构顶部导航布局

**目标文件：**

- `community-web/src/layouts/DefaultLayout.vue`

**要做内容：**

1. 品牌区升级
   - 优化 Logo 区视觉
   - 强化产品名与一句话说明

2. 导航分组
   - 主导航：首页、找人、发布
   - 功能导航：草稿、历史、聊天、通知
   - 用户区：我的、退出、资料入口

3. 聊天/通知图标化
   - 用图标 + badge，而不是纯文字

4. 用户区优化
   - 头像 + 昵称 + 下拉菜单

5. 页脚优化
   - 从阶段说明，改为简洁产品型页脚

**完成标准：**

- 头部更简洁、更像正式产品
- 用户操作路径更清晰

---

### 任务 5：首页改造成“社区首页”

**目标文件：**

- `community-web/src/views/home/HomeView.vue`

**要做内容：**

1. 首页双栏化
   - 左侧：帖子流主内容
   - 右侧：侧边信息区

2. 侧边栏建议模块
   - 快速发帖入口
   - 社区介绍
   - 热门标签
   - 推荐作者 / 活跃用户
   - 当前筛选说明

3. Hero 区文案优化
   - 减少“接口已接通”这类开发感表述
   - 增强“内容发现、讨论交流、加入社区”的产品语言

4. 筛选区轻量化
   - 搜索与排序布局更紧凑
   - 可考虑吸顶或固定在内容流上方

**完成标准：**

- 首页不再只是列表页
- 更具社区内容运营感

---

### 任务 6：帖子卡片精修

**目标文件：**

- `community-web/src/components/post/PostCard.vue`

**要做内容：**

1. 强化标题视觉层级
2. 优化作者信息展示
3. 统一封面图尺寸比例
4. 数据区改为 icon + 数字组合
5. 标签样式统一
6. 状态标签更明确
7. hover 态更自然

**完成标准：**

- 卡片更有内容产品感
- 列表浏览节奏更舒服

---

## P2：品牌入口页升级

### 任务 7：登录页重构

**目标文件：**

- `community-web/src/views/auth/LoginView.vue`

**要做内容：**

1. 改双栏布局
   - 左侧品牌信息
   - 右侧登录卡片

2. 增加背景装饰
   - 渐变
   - 光斑
   - 简单几何图形

3. 优化登录卡片
   - 标题、副标题、辅助文案更完整
   - 表单结构更精致

4. 优化跳转文案
   - 登录 / 注册切换更清楚

**完成标准：**

- 登录页摆脱“普通表单页”观感

---

### 任务 8：注册页与登录页统一设计

**目标文件：**

- `community-web/src/views/auth/RegisterView.vue`

**要做内容：**

- 与登录页复用统一布局
- 文案与表单节奏统一
- 减少两页设计差异

**完成标准：**

- 登录/注册形成同一套品牌入口体验

---

## P3：个人空间页面升级

> 当前状态：**已完成第一轮落地（2026-04-28）**

### 任务 9：个人中心页头重构

**状态：已完成第一轮实现（2026-04-28）**

**目标文件：**

- `community-web/src/views/user/UserCenterView.vue`

**要做内容：**

1. 将 Hero 区升级为“个人控制台”
2. 统计卡样式与全局统一
3. Tabs 区与内容区视觉分层
4. 资料编辑区做得更精致
5. 列表区统一 Panel 壳结构

**完成标准：**

- `/me` 更像核心产品页，而不是功能集合页

---

### 任务 10：用户主页精修

**状态：已完成第一轮实现（2026-04-28）**

**目标文件：**

- `community-web/src/views/user/UserProfileView.vue`

**要做内容：**

1. 强化头像 + 昵称 + 简介的个人名片感
2. 提高关注 / 私信按钮主次层级
3. 优化简介区与帖子区关系
4. 让公开帖子区更像个人内容流

**完成标准：**

- `/users/:userId` 更适合展示关系与内容

---

## P4：内容生产与阅读体验升级

### 任务 11：发帖页工作台化

**目标文件：**

- `community-web/src/views/post/PostPublishView.vue`

**要做内容：**

1. 强化编辑区 / 预览区双栏结构
2. 工具栏更像编辑器
3. 上传区、封面区、附件区卡片化
4. 保存状态更显眼
5. 桌面端预览区可考虑 sticky
6. 移动端编辑体验专项优化

**完成标准：**

- 发帖页更像“创作工作台”

---

### 任务 12：帖子详情页阅读体验优化

**目标文件：**

- `community-web/src/views/post/PostDetailView.vue`

**要做内容：**

1. 增强标题区视觉张力
2. 作者信息块更独立
3. 互动按钮分区更明确
4. Markdown 排版继续优化
5. 评论区层级更清晰

**完成标准：**

- 阅读体验更沉浸
- 评论互动区更清楚

---

## P5：移动端专项优化

### 任务 13：移动端导航方案

**涉及文件：**

- `community-web/src/layouts/DefaultLayout.vue`

**要做内容：**

- 小屏顶部导航收敛
- 抽屉导航或底部 TabBar 方案
- 发布按钮在移动端更突出

**完成标准：**

- 手机端导航更顺手

---

### 任务 14：移动端页面触控优化

**涉及页面：**

- `HomeView.vue`
- `PostCard.vue`
- `UserCenterView.vue`
- `UserProfileView.vue`
- `PostPublishView.vue`
- `PostDetailView.vue`

**要做内容：**

- 调整标题字号
- 调整卡片内边距
- 调整按钮点击区域
- 优化多操作按钮换行后的布局

**完成标准：**

- 不是简单单栏化，而是真正适合手机操作

---

## P6：状态与细节统一

> 当前状态：**已完成第一轮落地（2026-04-28）**

### 任务 15：统一空状态 / 加载状态 / 骨架屏

**状态：已完成第一轮实现（2026-04-28）**

**涉及范围：**

- 首页
- 搜索页
- 收藏/点赞页
- 历史页
- 评论区
- 通知页
- 聊天页

**要做内容：**

- 统一空状态文案风格
- 统一空状态卡片风格
- 统一加载骨架或 loading 容器样式

**完成标准：**

- 用户在所有页面的状态反馈体验更一致

---

### 任务 16：补齐图标与微动效

**状态：已完成第一轮实现（2026-04-28）**

**涉及范围：**

- 顶部导航
- 首页统计
- 帖子卡片
- 详情页互动区
- 个人中心

**要做内容：**

- 用统一图标体系替代部分纯文字
- 卡片 hover 轻动效
- Tabs 切换更顺滑
- 页面局部切换更自然

**完成标准：**

- 页面更有精致感，但不过度花哨

---

## 6. 建议执行顺序

### 第一轮（优先立即落地）

1. `src/styles/index.scss`
2. `src/layouts/DefaultLayout.vue`
3. `src/components/post/PostCard.vue`
4. `src/views/home/HomeView.vue`

### 第二轮

5. `src/views/auth/LoginView.vue`
6. `src/views/auth/RegisterView.vue`
7. `src/views/user/UserCenterView.vue`
8. `src/views/user/UserProfileView.vue`

### 第三轮

9. `src/views/post/PostPublishView.vue`
10. `src/views/post/PostDetailView.vue`
11. 移动端导航与触控优化
12. 空状态 / 骨架 / 图标 / 微动效统一

---

## 7. 验收标准

本轮 UI 优化完成后，至少要达到以下结果：

### 7.1 视觉统一

- 全局颜色、圆角、阴影、边框、标题层级统一
- 页面之间切换不再出现明显风格割裂

### 7.2 首页明显升级

- 首页不再只是帖子列表
- 具备社区首页的内容承载能力

### 7.3 核心页面更精致

- 登录/注册页品牌感更强
- 个人中心更像个人控制台
- 用户主页更像真实社区个人空间
- 发帖页和详情页更有产品完成度

### 7.4 移动端更顺手

- 导航路径清晰
- 按钮触控更舒服
- 布局不拥挤

### 7.5 工程可持续

- 新页面可以直接复用全局 token 与公共壳
- 后续迭代不再重复堆散乱样式

---

## 8. 配套文档建议

建议本轮 UI 优化同步沉淀以下文档：

### 8.1 `docs/前端UI优化清单.md`

用途：

- 本次执行清单
- 作为后续逐项打勾推进的落地文档

### 8.2 `docs/community-web UI 重构实施方案.md`

用途：

- 记录整体重构目标、范围、分期和方法

### 8.3 后续可补：`community-web/docs/ui-design-tokens.md`

用途：

- 记录颜色、圆角、阴影、字号、间距规范

---

## 9. 当前建议的下一步动作

建议下一步直接进入第一轮落地：

### 第一优先级

- 先改 `community-web/src/styles/index.scss`
- 再改 `community-web/src/layouts/DefaultLayout.vue`

原因：

- 这是全站收益最大的改动
- 改完后首页、个人中心、详情页等页面会整体抬升观感
- 后续再改页面时不会继续重复写零散样式

---

## 10. 备注

本清单基于当前项目实际页面结构整理，不建议脱离现有页面体系另起一套新 UI 架构。后续优化应继续遵循：

- 保持单体社区项目当前信息架构
- 保持 `Arco Design Vue` 为主组件库
- 保持增量演进，而不是推翻重写

