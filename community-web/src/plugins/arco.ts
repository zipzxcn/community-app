import type { App } from 'vue'
import ArcoVue from '@arco-design/web-vue'
import '@arco-design/web-vue/dist/arco.css'

export function installArco(app: App) {
  app.use(ArcoVue)
}
