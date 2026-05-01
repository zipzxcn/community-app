/**
 * 前端应用入口：
 * 1) createApp 创建 Vue 根实例。
 * 2) Pinia 负责全局状态，例如登录态、通知未读、聊天连接状态。
 * 3) Vue Router 承载单页应用路由切换。
 * 4) Arco Design Vue 负责 UI 组件体系。
 */
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { installArco } from './plugins/arco'
import './styles/index.scss'

const app = createApp(App)

// 注册 Pinia 后，页面与 API 拦截器都能共享统一状态。
app.use(createPinia())
// 注册路由，负责页面访问控制与懒加载。
app.use(router)
// 安装 Arco UI 组件库，统一按钮、表单、消息提示等视觉规范。
installArco(app)
app.mount('#app')
