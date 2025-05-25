# API 文档

## 目录

- [用户相关接口](#用户相关接口)
- [登录相关接口](#登录相关接口)
- [消息相关接口](#消息相关接口)
- [会话相关接口](#会话相关接口)
- [好友关系相关接口](#好友关系相关接口)
- [WebSocket相关接口](#websocket相关接口)


## 用户相关接口

### 获取当前登录用户信息
- **URL**: `/api/user/current`
- **方法**: GET
- **描述**: 获取当前登录用户的信息。
- **示例**:
  ```bash
  curl -X GET "http://localhost:8080/api/user/current" -H "Authorization: Bearer <token>"
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": {
      "id": 1,
      "nickname": "用户昵称",
      "phone": "1234567890",
      "avatar": "avatar_url",
      "status": 0
    }
  }
  ```

### 根据手机号获取用户信息
- **URL**: `/api/user/phone/{phone}`
- **方法**: GET
- **描述**: 根据手机号获取用户信息，仅限当前登录用户。
- **示例**:
  ```bash
  curl -X GET "http://localhost:8080/api/user/phone/1234567890" -H "Authorization: Bearer <token>"
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": {
      "id": 1,
      "nickname": "用户昵称",
      "phone": "1234567890",
      "avatar": "avatar_url",
      "status": 0
    }
  }
  ```

### 根据ID获取用户信息
- **URL**: `/api/user/{id}`
- **方法**: GET
- **描述**: 根据用户ID获取用户信息，仅限当前登录用户。
- **示例**:
  ```bash
  curl -X GET "http://localhost:8080/api/user/1" -H "Authorization: Bearer <token>"
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": {
      "id": 1,
      "nickname": "用户昵称",
      "phone": "1234567890",
      "avatar": "avatar_url",
      "status": 0
    }
  }
  ```

### 更新用户信息
- **URL**: `/api/user`
- **方法**: PUT
- **描述**: 更新当前登录用户的信息。
- **示例**:
  ```bash
  curl -X PUT "http://localhost:8080/api/user" -H "Authorization: Bearer <token>" -H "Content-Type: application/json" -d '{"nickname": "新昵称"}'
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "更新成功"
  }
  ```

### 更新用户在线状态
- **URL**: `/api/user/status`
- **方法**: PUT
- **描述**: 更新用户的在线状态。
- **示例**:
  ```bash
  curl -X PUT "http://localhost:8080/api/user/status" -H "Authorization: Bearer <token>"
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "状态更新成功"
  }
  ```

## 登录相关接口

### 用户登录
- **URL**: `/api/login`
- **方法**: POST
- **描述**: 用户登录接口，验证手机号和密码。
- **示例**:
  ```bash
  curl -X POST "http://localhost:8080/api/login" -H "Content-Type: application/json" -d '{"phone": "1234567890", "password": "password"}'
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "登录成功",
    "data": {
      "token": "jwt_token"
    }
  }
  ```

## 消息相关接口

### 发送私聊消息
- **URL**: `/api/message/private/send`
- **方法**: POST
- **描述**: 发送私聊消息。
- **示例**:
  ```bash
  curl -X POST "http://localhost:8080/api/message/private/send" -H "Authorization: Bearer <token>" -H "Content-Type: application/json" -d '{"receiverPhone": "0987654321", "contentType": 1, "content": "Hello"}'
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "消息发送成功",
    "data": {
      "messageId": 123,
      "timestamp": "2023-10-10T10:00:00Z"
    }
  }
  ```

### 发送群聊消息
- **URL**: `/api/message/group/send`
- **方法**: POST
- **描述**: 发送群聊消息。
- **示例**:
  ```bash
  curl -X POST "http://localhost:8080/api/message/group/send" -H "Authorization: Bearer <token>" -H "Content-Type: application/json" -d '{"groupId": 1, "contentType": 1, "content": "Hello Group"}'
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "消息发送成功",
    "data": {
      "messageId": 124,
      "timestamp": "2023-10-10T10:01:00Z"
    }
  }
  ```

### 获取私聊消息列表
- **URL**: `/api/message/private/{otherPhone}`
- **方法**: GET
- **描述**: 获取与指定手机号的私聊消息列表。
- **示例**:
  ```bash
  curl -X GET "http://localhost:8080/api/message/private/0987654321" -H "Authorization: Bearer <token>"
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": [
      {
        "messageId": 123,
        "sender": "1234567890",
        "content": "Hello",
        "timestamp": "2023-10-10T10:00:00Z"
      }
    ]
  }
  ```

### 获取群聊消息列表
- **URL**: `/api/message/group/{groupId}`
- **方法**: GET
- **描述**: 获取指定群组的消息列表。
- **示例**:
  ```bash
  curl -X GET "http://localhost:8080/api/message/group/1" -H "Authorization: Bearer <token>"
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": [
      {
        "messageId": 124,
        "sender": "1234567890",
        "content": "Hello Group",
        "timestamp": "2023-10-10T10:01:00Z"
      }
    ]
  }
  ```

### 标记消息为已读
- **URL**: `/api/message/read/{messageId}`
- **方法**: PUT
- **描述**: 标记指定消息为已读。
- **示例**:
  ```bash
  curl -X PUT "http://localhost:8080/api/message/read/1" -H "Authorization: Bearer <token>"
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "标记成功"
  }
  ```

### 撤回消息
- **URL**: `/api/message/recall/{messageId}`
- **方法**: PUT
- **描述**: 撤回指定消息。
- **示例**:
  ```bash
  curl -X PUT "http://localhost:8080/api/message/recall/1" -H "Authorization: Bearer <token>"
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "撤回成功"
  }
  ```

### 删除消息
- **URL**: `/api/message/{messageId}`
- **方法**: DELETE
- **描述**: 删除指定消息。
- **示例**:
  ```bash
  curl -X DELETE "http://localhost:8080/api/message/1" -H "Authorization: Bearer <token>"
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "删除成功"
  }
  ```

### 获取未读消息数
- **URL**: `/api/message/unread/count`
- **方法**: GET
- **描述**: 获取未读消息的数量。
- **示例**:
  ```bash
  curl -X GET "http://localhost:8080/api/message/unread/count" -H "Authorization: Bearer <token>"
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": 5
  }
  ```

### 获取离线消息
- **URL**: `/api/message/offline`
- **方法**: GET
- **描述**: 获取离线消息列表。
- **示例**:
  ```bash
  curl -X GET "http://localhost:8080/api/message/offline" -H "Authorization: Bearer <token>"
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": [
      {
        "messageId": 125,
        "sender": "1234567890",
        "content": "Offline message",
        "timestamp": "2023-10-10T09:00:00Z"
      }
    ]
  }
  ```

### 确认接收离线消息
- **URL**: `/api/message/offline/confirm`
- **方法**: POST
- **描述**: 确认接收离线消息。
- **示例**:
  ```bash
  curl -X POST "http://localhost:8080/api/message/offline/confirm" -H "Authorization: Bearer <token>" -H "Content-Type: application/json" -d '{"messageIds": [1, 2, 3]}'
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "确认成功"
  }
  ```

### 获取消息详情
- **URL**: `/api/message/{messageId}`
- **方法**: GET
- **描述**: 获取指定消息的详情。
- **示例**:
  ```bash
  curl -X GET "http://localhost:8080/api/message/1" -H "Authorization: Bearer <token>"
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": {
      "messageId": 123,
      "sender": "1234567890",
      "content": "Hello",
      "timestamp": "2023-10-10T10:00:00Z"
    }
  }
  ```

## 会话相关接口

### 获取用户的会话列表
- **URL**: `/api/conversation/list`
- **方法**: GET
- **描述**: 获取当前用户的会话列表。
- **示例**:
  ```bash
  curl -X GET "http://localhost:8080/api/conversation/list" -H "Authorization: Bearer <token>"
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": [
      {
        "conversationId": 1,
        "title": "与用户123的会话",
        "lastMessage": "Hello",
        "timestamp": "2023-10-10T10:00:00Z"
      }
    ]
  }
  ```

### 获取会话详情
- **URL**: `/api/conversation/{conversationId}`
- **方法**: GET
- **描述**: 获取指定会话的详情。
- **示例**:
  ```bash
  curl -X GET "http://localhost:8080/api/conversation/1" -H "Authorization: Bearer <token>"
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": {
      "conversationId": 1,
      "title": "与用户123的会话",
      "messages": [
        {
          "messageId": 123,
          "content": "Hello",
          "timestamp": "2023-10-10T10:00:00Z"
        }
      ]
    }
  }
  ```

### 创建或获取私聊会话
- **URL**: `/api/conversation/private`
- **方法**: POST
- **描述**: 创建或获取与指定用户的私聊会话。
- **示例**:
  ```bash
  curl -X POST "http://localhost:8080/api/conversation/private" -H "Authorization: Bearer <token>" -H "Content-Type: application/json" -d '{"targetUserId": 2}'
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": {
      "conversationId": 2,
      "title": "与用户456的会话"
    }
  }
  ```

### 创建或获取群聊会话
- **URL**: `/api/conversation/group`
- **方法**: POST
- **描述**: 创建或获取指定群组的会话。
- **示例**:
  ```bash
  curl -X POST "http://localhost:8080/api/conversation/group" -H "Authorization: Bearer <token>" -H "Content-Type: application/json" -d '{"groupId": 1}'
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": {
      "conversationId": 3,
      "title": "群聊1"
    }
  }
  ```

### 删除会话
- **URL**: `/api/conversation/{conversationId}`
- **方法**: DELETE
- **描述**: 删除指定会话。
- **示例**:
  ```bash
  curl -X DELETE "http://localhost:8080/api/conversation/1" -H "Authorization: Bearer <token>"
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "删除成功"
  }
  ```

### 置顶会话
- **URL**: `/api/conversation/{conversationId}/top`
- **方法**: PUT
- **描述**: 设置会话置顶。
- **示例**:
  ```bash
  curl -X PUT "http://localhost:8080/api/conversation/1/top" -H "Authorization: Bearer <token>" -H "Content-Type: application/json" -d '{"isTop": true}'
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "置顶成功"
  }
  ```

### 设置会话免打扰
- **URL**: `/api/conversation/{conversationId}/mute`
- **方法**: PUT
- **描述**: 设置会话免打扰。
- **示例**:
  ```bash
  curl -X PUT "http://localhost:8080/api/conversation/1/mute" -H "Authorization: Bearer <token>" -H "Content-Type: application/json" -d '{"isMuted": true}'
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "设置成功"
  }
  ```

### 清空会话消息
- **URL**: `/api/conversation/{conversationId}/clear`
- **方法**: PUT
- **描述**: 清空指定会话的消息。
- **示例**:
  ```bash
  curl -X PUT "http://localhost:8080/api/conversation/1/clear" -H "Authorization: Bearer <token>"
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "清空成功"
  }
  ```

### 标记会话已读
- **URL**: `/api/conversation/{conversationId}/read`
- **方法**: PUT
- **描述**: 标记指定会话为已读。
- **示例**:
  ```bash
  curl -X PUT "http://localhost:8080/api/conversation/1/read" -H "Authorization: Bearer <token>"
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "标记成功"
  }
  ```

## 好友关系相关接口

### 获取好友列表
- **URL**: `/api/friendship/list`
- **方法**: GET
- **描述**: 获取当前用户的好友列表。
- **示例**:
  ```bash
  curl -X GET "http://localhost:8080/api/friendship/list" -H "Authorization: Bearer <token>"
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": [
      {
        "friendId": 3,
        "nickname": "朋友",
        "status": 0
      }
    ]
  }
  ```

### 添加好友
- **URL**: `/api/friendship/add`
- **方法**: POST
- **描述**: 添加好友。
- **示例**:
  ```bash
  curl -X POST "http://localhost:8080/api/friendship/add" -H "Authorization: Bearer <token>" -H "Content-Type: application/json" -d '{"friendId": 3, "remark": "朋友", "groupId": 1}'
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "添加成功"
  }
  ```

### 删除好友
- **URL**: `/api/friendship/{friendId}`
- **方法**: DELETE
- **描述**: 删除指定好友。
- **示例**:
  ```bash
  curl -X DELETE "http://localhost:8080/api/friendship/3" -H "Authorization: Bearer <token>"
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "删除成功"
  }
  ```

### 更新好友备注
- **URL**: `/api/friendship/remark`
- **方法**: PUT
- **描述**: 更新好友备注。
- **示例**:
  ```bash
  curl -X PUT "http://localhost:8080/api/friendship/remark" -H "Authorization: Bearer <token>" -H "Content-Type: application/json" -d '{"friendId": 3, "remark": "新备注"}'
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "更新成功"
  }
  ```

### 更新好友分组
- **URL**: `/api/friendship/group`
- **方法**: PUT
- **描述**: 更新好友分组。
- **示例**:
  ```bash
  curl -X PUT "http://localhost:8080/api/friendship/group" -H "Authorization: Bearer <token>" -H "Content-Type: application/json" -d '{"friendId": 3, "groupId": 2}'
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "更新成功"
  }
  ```

### 更新好友状态
- **URL**: `/api/friendship/status`
- **方法**: PUT
- **描述**: 更新好友状态（拉黑/取消拉黑）。
- **示例**:
  ```bash
  curl -X PUT "http://localhost:8080/api/friendship/status" -H "Authorization: Bearer <token>" -H "Content-Type: application/json" -d '{"friendId": 3, "status": 1}'
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "更新成功"
  }
  ```

### 获取好友关系
- **URL**: `/api/friendship/{friendId}`
- **方法**: GET
- **描述**: 获取指定好友的关系。
- **示例**:
  ```bash
  curl -X GET "http://localhost:8080/api/friendship/3" -H "Authorization: Bearer <token>"
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": {
      "friendId": 3,
      "nickname": "朋友",
      "status": 0
    }
  }
  ```

### 检查是否为好友
- **URL**: `/api/friendship/check/{friendId}`
- **方法**: GET
- **描述**: 检查是否为好友关系。
- **示例**:
  ```bash
  curl -X GET "http://localhost:8080/api/friendship/check/3" -H "Authorization: Bearer <token>"
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": true
  }
  ```

## WebSocket相关接口

### 处理私聊消息
- **路径**: `/private.message`
- **描述**: 处理私聊消息的WebSocket接口。
- **示例**:
  ```javascript
  // 使用WebSocket发送私聊消息
  socket.send(JSON.stringify({
    destination: "/app/private.message",
    body: {
      receiverId: 2,
      contentType: 1,
      content: "Hello"
    }
  }));
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "消息发送成功"
  }
  ```

### 处理群聊消息
- **路径**: `/group.message`
- **描述**: 处理群聊消息的WebSocket接口。
- **示例**:
  ```javascript
  // 使用WebSocket发送群聊消息
  socket.send(JSON.stringify({
    destination: "/app/group.message",
    body: {
      groupId: 1,
      contentType: 1,
      content: "Hello Group"
    }
  }));
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "消息发送成功"
  }
  ```

### 处理消息已读回执
- **路径**: `/message.read`
- **描述**: 处理消息已读回执的WebSocket接口。
- **示例**:
  ```javascript
  // 使用WebSocket发送已读回执
  socket.send(JSON.stringify({
    destination: "/app/message.read",
    body: {
      messageId: 1
    }
  }));
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "已读回执成功"
  }
  ```

### 处理用户在线状态
- **路径**: `/user.status`
- **描述**: 处理用户在线状态的WebSocket接口。
- **示例**:
  ```javascript
  // 使用WebSocket更新用户在线状态
  socket.send(JSON.stringify({
    destination: "/app/user.status",
    body: {}
  }));
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "状态更新成功"
  }
  ```

### 处理消息撤回
- **路径**: `/message.recall`
- **描述**: 处理消息撤回的WebSocket接口。
- **示例**:
  ```javascript
  // 使用WebSocket撤回消息
  socket.send(JSON.stringify({
    destination: "/app/message.recall",
    body: {
      messageId: 1
    }
  }));
  ```
- **返回示例**:
  ```json
  {
    "code": 200,
    "message": "撤回成功"
  }
  ```