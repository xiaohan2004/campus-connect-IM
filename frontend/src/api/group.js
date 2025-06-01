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

// 添加群成员（单个）
export function addGroupMember(groupId, data) {
  return request({
    url: `/api/group/${groupId}/member`,
    method: 'post',
    data
  });
}

// 添加群成员（批量）
export function addGroupMembers(groupId, data) {
  return request({
    url: `/api/group/${groupId}/members`,
    method: 'post',
    data
  });
}

// 移除群成员
export function removeGroupMember(groupId, userId) {
  return request({
    url: `/api/group/${groupId}/member/${userId}`,
    method: 'delete'
  });
}

// 用户主动退出群组
export function quitGroup(groupId) {
  return request({
    url: `/api/group/${groupId}/quit`,
    method: 'post'
  });
}

// 设置群成员角色
export function setGroupMemberRole(groupId, userId, data) {
  return request({
    url: `/api/group/${groupId}/member/${userId}/role`,
    method: 'put',
    data
  });
}

// 设置群内昵称
export function setGroupMemberNickname(groupId, data) {
  return request({
    url: `/api/group/${groupId}/nickname`,
    method: 'put',
    data
  });
}

// 禁言群成员
export function muteGroupMember(groupId, userId, data) {
  return request({
    url: `/api/group/${groupId}/member/${userId}/mute`,
    method: 'put',
    data
  });
}

// 取消禁言
export function unmuteGroupMember(groupId, userId) {
  return request({
    url: `/api/group/${groupId}/member/${userId}/unmute`,
    method: 'put'
  });
}

// 获取群成员列表（含角色、昵称等）
export function getGroupMembers(groupId) {
  return request({
    url: `/api/group/${groupId}/members`,
    method: 'get'
  });
}

// 获取群成员用户信息列表（只返回 User 信息）
export function getGroupMemberUsers(groupId) {
  return request({
    url: `/api/group/${groupId}/users`,
    method: 'get'
  });
}

// 获取当前用户加入的所有群组
export function getUserGroups() {
  return request({
    url: '/api/group/my',
    method: 'get'
  });
}

// 检查当前用户是否在指定群组中
export function checkUserInGroup(groupId) {
  return request({
    url: `/api/group/${groupId}/check`,
    method: 'get'
  });
}

// 获取所有群组
export function getAllGroups() {
  return request({
    url: '/api/group/all',
    method: 'get'
  });
} 