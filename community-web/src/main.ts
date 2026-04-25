import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { installArco } from './plugins/arco'
import './styles/index.scss'

const app = createApp(App)

app.use(createPinia())
app.use(router)
installArco(app)
app.mount('#app')
