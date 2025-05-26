import request from '@/utils/request';

// 创建群组
export function createGroup(data) {
  return request({
    url: '/api/group/create',
    method: 'post',
    data
  });
}

// 解散群组
export function disbandGroup(groupId) {
  return request({
    url: `/api/group/${groupId}`,
    method: 'delete'
  });
}

// 更新群组信息
export function updateGroupInfo(groupId, data) {
  return request({
    url: `/api/group/${groupId}`,
    method: 'put',
    data
  });
}

// 获取群组信息
export function getGroupInfo(groupId) {
  return request({
    url: `/api/group/${groupId}`,
    method: 'get'
  });
}

// 添加群成员
export function addGroupMember(groupId, userId) {
  return request({
    url: `/api/group/${groupId}/member`,
    method: 'post',
    data: { userId }
  });
}

// 批量添加群成员
export function addGroupMembers(groupId, userIds) {
  return request({
    url: `/api/group/${groupId}/members`,
    method: 'post',
    data: { userIds }
  });
}

// 移除群成员
export function removeGroupMember(groupId, userId) {
  return request({
    url: `/api/group/${groupId}/member/${userId}`,
    method: 'delete'
  });
}

// 退出群组
export function quitGroup(groupId) {
  return request({
    url: `/api/group/${groupId}/quit`,
    method: 'post'
  });
}

// 设置群成员角色
export function setGroupMemberRole(groupId, userId, role) {
  return request({
    url: `/api/group/${groupId}/member/${userId}/role`,
    method: 'put',
    data: { role }
  });
}

// 设置群成员昵称
export function setGroupMemberNickname(groupId, nickname) {
  return request({
    url: `/api/group/${groupId}/nickname`,
    method: 'put',
    data: { nickname }
  });
}

// 禁言群成员
export function muteGroupMember(groupId, userId, duration) {
  return request({
    url: `/api/group/${groupId}/member/${userId}/mute`,
    method: 'put',
    data: { duration }
  });
}

// 解除群成员禁言
export function unmuteGroupMember(groupId, userId) {
  return request({
    url: `/api/group/${groupId}/member/${userId}/unmute`,
    method: 'put'
  });
}

// 获取群成员列表
export function getGroupMembers(groupId) {
  return request({
    url: `/api/group/${groupId}/members`,
    method: 'get'
  });
}

// 获取群成员用户信息列表
export function getGroupMemberUsers(groupId) {
  return request({
    url: `/api/group/${groupId}/users`,
    method: 'get'
  });
}

// 获取用户加入的群组列表
export function getUserGroups() {
  return request({
    url: '/api/group/my',
    method: 'get'
  });
}

// 检查用户是否在群组中
export function checkUserInGroup(groupId) {
  return request({
    url: `/api/group/${groupId}/check`,
    method: 'get'
  });
} 