/**
 * Arco Design Vue 安装入口。
 * 该模块把第三方 UI 依赖收口到一处，便于后续统一切换组件库或做按需优化。
 */
import type { App } from 'vue'
import ArcoVue from '@arco-design/web-vue'
import '@arco-design/web-vue/dist/arco.css'

export function installArco(app: App) {
  app.use(ArcoVue)
}
