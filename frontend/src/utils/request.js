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
    
    // 只有当响应码不是200时，才认为请求错误
    if (res.code !== 200) {
      // 获取错误信息，优先使用 msg，其次是 message
      const errorMsg = res.msg || res.message || '请求错误';
      
      // 401: 未登录或token过期
      if (res.code === 401) {
        // 清除token并跳转到登录页
        localStorage.removeItem('token');
        router.push('/login');
      }
      
      // 返回一个带有后端错误信息的错误对象
      const error = new Error(errorMsg);
      error.response = { data: res };
      return Promise.reject(error);
    } else {
      return res;
    }
  },
  error => {
    console.log('请求错误:', error);
    
    // 尝试从错误响应中获取错误信息
    let errorMsg = '网络错误，请稍后重试';
    
    if (error.response && error.response.data) {
      // 优先使用后端返回的错误信息
      errorMsg = error.response.data.msg || error.response.data.message || error.message || errorMsg;
    } else if (error.message) {
      errorMsg = error.message;
    }
    
    // 确保错误对象包含完整的响应信息
    if (!error.response) {
      error.response = { data: { msg: errorMsg } };
    }
    
    return Promise.reject(error);
  }
);

export default service; 