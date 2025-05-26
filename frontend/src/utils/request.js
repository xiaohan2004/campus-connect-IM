import axios from 'axios';
import { ElMessage } from 'element-plus';
import router from '@/router';

// 创建axios实例
const service = axios.create({
  baseURL: 'http://localhost:8080', // 统一前缀
  timeout: 10000 // 请求超时时间
});

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    console.log(error);
    return Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data;
    // 如果响应码不是200，认为请求错误
    if (res.code !== 200) {
      ElMessage({
        message: res.message || '请求错误',
        type: 'error',
        duration: 5 * 1000
      });

      // 401: 未登录或token过期
      if (res.code === 401) {
        // 清除token并跳转到登录页
        localStorage.removeItem('token');
        router.push('/login');
      }
      return Promise.reject(new Error(res.message || '请求错误'));
    } else {
      return res;
    }
  },
  error => {
    console.log('请求错误:', error);
    ElMessage({
      message: error.message || '请求错误',
      type: 'error',
      duration: 5 * 1000
    });
    return Promise.reject(error);
  }
);

export default service; 