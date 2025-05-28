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

/**
 * 用户登录
 * @param {Object} data - 登录信息
 * @param {String} data.phone - 手机号
 * @param {String} data.password - 密码
 * @returns {Promise}
 */
export function login(data) {
  return request({
    url: '/api/user/login',
    method: 'post',
    data
  });
}

/**
 * 用户注册
 * @param {Object} data - 注册信息
 * @param {String} data.nickname - 昵称
 * @param {String} data.userType - 用户身份(STUDENT-校内用户, MERCHANT-商家)
 * @param {String} data.phone - 手机号
 * @param {String} data.email - 邮箱
 * @param {String} data.verificationCode - 验证码
 * @param {String} data.password - 密码
 * @returns {Promise}
 */
export function register(data) {
  return request({
    url: '/api/register',
    method: 'post',
    data
  });
}

/**
 * 发送短信验证码
 * @param {String} phone - 手机号
 * @param {String} type - 验证码类型 (register-注册, reset-重置密码)
 * @returns {Promise}
 */
export function sendSmsCode(phone, type = 'register') {
  return request({
    url: '/api/user/sendSmsCode',
    method: 'post',
    data: {
      phone,
      type
    }
  });
}

/**
 * 发送邮箱验证码
 * @param {String} email - 邮箱地址
 * @param {String} type - 验证码类型 (register-注册, reset-重置密码)
 * @returns {Promise}
 */
export function sendEmailCode(email, type = 'register') {
  return request({
    url: '/api/sendVerificationCode',
    method: 'post',
    data: {
      email,
      type
    }
  });
}

/**
 * 重置密码
 * @param {Object} data - 重置密码信息
 * @param {String} data.phone - 手机号
 * @param {String} data.email - 邮箱地址
 * @param {String} data.verificationCode - 验证码
 * @param {String} data.newPassword - 新密码
 * @returns {Promise}
 */
export function resetPassword(data) {
  return request({
    url: '/api/resetPassword',
    method: 'post',
    data
  });
}

/**
 * 获取用户信息
 * @returns {Promise}
 */
export function getUserInfo() {
  return request({
    url: '/api/user/info',
    method: 'get'
  });
}

/**
 * 更新用户信息
 * @param {Object} data - 用户信息
 * @returns {Promise}
 */
export function updateUserInfo(data) {
  return request({
    url: '/api/user/update',
    method: 'put',
    data
  });
}

/**
 * 更新用户头像
 * @param {FormData} formData - 包含头像文件的表单数据
 * @returns {Promise}
 */
export function updateAvatar(formData) {
  return request({
    url: '/api/user/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

/**
 * 退出登录
 * @returns {Promise}
 */
export function logout() {
  return request({
    url: '/api/user/logout',
    method: 'post'
  });
}

/**
 * 修改密码
 * @param {Object} data - 密码信息
 * @param {String} data.oldPassword - 旧密码
 * @param {String} data.newPassword - 新密码
 * @returns {Promise}
 */
export function changePassword(data) {
  return request({
    url: '/api/user/changePassword',
    method: 'post',
    data
  });
} 