import request from '@/utils/request';

// 发送私聊消息
export function sendPrivateMessage(data) {
  return request({
    url: '/api/message/private/send',
    method: 'post',
    data
  });
}

// 发送群聊消息
export function sendGroupMessage(data) {
  return request({
    url: '/api/message/group/send',
    method: 'post',
    data
  });
}

// 获取私聊消息列表
export function getPrivateMessages(otherPhone) {
  return request({
    url: `/api/message/private/${otherPhone}`,
    method: 'get'
  });
}

// 获取群聊消息列表
export function getGroupMessages(groupId) {
  return request({
    url: `/api/message/group/${groupId}`,
    method: 'get'
  });
}

// 标记消息为已读
export function markMessageAsRead(messageId) {
  return request({
    url: `/api/message/read/${messageId}`,
    method: 'put'
  });
}

// 撤回消息
export function recallMessage(messageId) {
  return request({
    url: `/api/message/recall/${messageId}`,
    method: 'put'
  });
}

// 删除消息
export function deleteMessage(messageId) {
  return request({
    url: `/api/message/${messageId}`,
    method: 'delete'
  });
}

// 获取未读消息数
export function getUnreadMessageCount() {
  return request({
    url: '/api/message/unread/count',
    method: 'get'
  });
}

// 获取离线消息
export function getOfflineMessages() {
  return request({
    url: '/api/message/offline',
    method: 'get'
  });
}

// 确认接收离线消息
export function confirmOfflineMessages(data) {
  return request({
    url: '/api/message/offline/confirm',
    method: 'post',
    data
  });
}

// 获取消息详情
export function getMessageDetail(messageId) {
  return request({
    url: `/api/message/${messageId}`,
    method: 'get'
  });
} 