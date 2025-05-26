import { createApp } from 'vue';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import App from './App.vue';
import router from './router';
import store from './store';
import GlobalComponents from './components';
import './assets/styles/global.scss';

// 创建Vue应用
const app = createApp(App);

// 使用插件
app.use(ElementPlus);
app.use(router);
app.use(store);
app.use(GlobalComponents);

// 全局错误处理
app.config.errorHandler = (err, vm, info) => {
  console.error('Vue全局错误:', err, info);
};

// 挂载应用
app.mount('#app'); 