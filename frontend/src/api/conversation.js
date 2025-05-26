import request from '@/utils/request';

// 获取用户的会话列表
export function getConversationList() {
  return request({
    url: '/api/conversation/list',
    method: 'get'
  });
}

// 获取会话详情
export function getConversationDetail(conversationId) {
  return request({
    url: `/api/conversation/${conversationId}`,
    method: 'get'
  });
}

// 创建或获取私聊会话
export function createPrivateConversation(data) {
  return request({
    url: '/api/conversation/private',
    method: 'post',
    data
  });
}

// 创建或获取群聊会话
export function createGroupConversation(data) {
  return request({
    url: '/api/conversation/group',
    method: 'post',
    data
  });
}

// 删除会话
export function deleteConversation(conversationId) {
  return request({
    url: `/api/conversation/${conversationId}`,
    method: 'delete'
  });
}

// 置顶会话
export function topConversation(conversationId, data) {
  return request({
    url: `/api/conversation/${conversationId}/top`,
    method: 'put',
    data
  });
}

// 设置会话免打扰
export function muteConversation(conversationId, data) {
  return request({
    url: `/api/conversation/${conversationId}/mute`,
    method: 'put',
    data
  });
}

// 清空会话消息
export function clearConversation(conversationId) {
  return request({
    url: `/api/conversation/${conversationId}/clear`,
    method: 'put'
  });
}

// 标记会话已读
export function markConversationAsRead(conversationId) {
  return request({
    url: `/api/conversation/${conversationId}/read`,
    method: 'put'
  });
} 