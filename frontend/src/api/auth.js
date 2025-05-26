import request from '@/utils/request';

// 用户登录
export function login(data) {
  return request({
    url: '/api/login',
    method: 'post',
    data
  });
} 