import request from '@/utils/request';

// 获取好友列表
export function getFriendList() {
  return request({
    url: '/api/friendship/list',
    method: 'get'
  });
}

// 添加好友
export function addFriend(data) {
  return request({
    url: '/api/friendship/add',
    method: 'post',
    data
  });
}

// 删除好友
export function deleteFriend(friendId) {
  return request({
    url: `/api/friendship/${friendId}`,
    method: 'delete'
  });
}

// 更新好友备注
export function updateFriendRemark(data) {
  return request({
    url: '/api/friendship/remark',
    method: 'put',
    data
  });
}

// 更新好友分组
export function updateFriendGroup(data) {
  return request({
    url: '/api/friendship/group',
    method: 'put',
    data
  });
}

// 更新好友状态
export function updateFriendStatus(data) {
  return request({
    url: '/api/friendship/status',
    method: 'put',
    data
  });
}

// 获取好友关系
export function getFriendship(friendId) {
  return request({
    url: `/api/friendship/${friendId}`,
    method: 'get'
  });
}

// 检查是否为好友
export function checkFriendship(friendId) {
  return request({
    url: `/api/friendship/check/${friendId}`,
    method: 'get'
  });
} 