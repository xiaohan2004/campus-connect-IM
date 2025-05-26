import request from '@/utils/request';

// 获取当前登录用户信息
export function getCurrentUser() {
  return request({
    url: '/api/user/current',
    method: 'get'
  });
}

// 根据手机号获取用户信息
export function getUserByPhone(phone) {
  return request({
    url: `/api/user/phone/${phone}`,
    method: 'get'
  });
}

// 根据ID获取用户信息
export function getUserById(id) {
  return request({
    url: `/api/user/${id}`,
    method: 'get'
  });
}

// 更新用户信息
export function updateUser(data) {
  return request({
    url: '/api/user',
    method: 'put',
    data
  });
}

// 更新用户在线状态
export function updateUserStatus() {
  return request({
    url: '/api/user/status',
    method: 'put'
  });
} 