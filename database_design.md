# 即时通讯系统数据库设计

## 1. 设计概述

根据应用规划文档的要求，本即时通讯系统需要支持私聊、群聊、好友管理等核心功能，同时系统不提供用户注册登录功能，而是通过JWT中携带的phone字段来识别和验证用户。数据库设计需要满足以下要点：

1. 用户信息管理（仅存储必要信息，不包含认证逻辑）
2. 好友关系管理
3. 群组及群成员管理
4. 消息存储与管理（包括离线消息）
5. 会话管理

## 2. 数据库选型

- **关系型数据库**：MySQL 8.0
  - 用于存储结构化数据，如用户信息、好友关系、群组信息等
  - 提供强大的事务支持和数据一致性保证

- **缓存数据库**：Redis 6.x
  - 用于存储临时数据，如在线状态、未读消息计数等
  - 提供高性能的消息队列功能，支持实时消息传递

## 3. 表结构设计

### 3.1 用户表 (user)

存储用户的基本信息，不包含认证信息（由JWT处理）。

```sql
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号（唯一标识）',
  `nickname` VARCHAR(50) NOT NULL COMMENT '用户昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '用户状态：0-正常，1-禁用',
  `last_active_time` DATETIME DEFAULT NULL COMMENT '最后活跃时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

### 3.2 好友关系表 (friendship)

存储用户之间的好友关系。

```sql
CREATE TABLE `friendship` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关系ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `friend_id` BIGINT NOT NULL COMMENT '好友ID',
  `remark` VARCHAR(50) DEFAULT NULL COMMENT '好友备注',
  `group_id` BIGINT DEFAULT NULL COMMENT '好友分组ID',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-正常，1-拉黑',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_friend` (`user_id`, `friend_id`),
  KEY `idx_friend_id` (`friend_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友关系表';
```

### 3.3 好友分组表 (friend_group)

存储用户创建的好友分组。

```sql
CREATE TABLE `friend_group` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分组ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分组名称',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友分组表';
```

### 3.4 好友申请表 (friend_request)

存储好友申请记录。

```sql
CREATE TABLE `friend_request` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `from_user_id` BIGINT NOT NULL COMMENT '申请人ID',
  `to_user_id` BIGINT NOT NULL COMMENT '接收人ID',
  `message` VARCHAR(255) DEFAULT NULL COMMENT '申请消息',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待处理，1-已接受，2-已拒绝',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_from_user_id` (`from_user_id`),
  KEY `idx_to_user_id` (`to_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友申请表';
```

### 3.5 群组表 (group)

存储群组信息。

```sql
CREATE TABLE `chat_group` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '群组ID',
  `name` VARCHAR(100) NOT NULL COMMENT '群组名称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '群头像URL',
  `creator_id` BIGINT NOT NULL COMMENT '创建者ID',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '群组描述',
  `announcement` TEXT DEFAULT NULL COMMENT '群公告',
  `member_count` INT NOT NULL DEFAULT 0 COMMENT '成员数量',
  `max_member_count` INT NOT NULL DEFAULT 200 COMMENT '最大成员数量',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-正常，1-解散',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_creator_id` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群组表';
```

### 3.6 群成员表 (group_member)

存储群组成员信息。

```sql
CREATE TABLE `group_member` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `group_id` BIGINT NOT NULL COMMENT '群组ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '群内昵称',
  `role` TINYINT NOT NULL DEFAULT 0 COMMENT '角色：0-普通成员，1-管理员，2-群主',
  `mute_end_time` DATETIME DEFAULT NULL COMMENT '禁言结束时间',
  `join_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_user` (`group_id`, `user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群成员表';
```

### 3.7 会话表 (conversation)

存储用户的会话信息，包括私聊和群聊。

```sql
CREATE TABLE `conversation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `conversation_type` TINYINT NOT NULL COMMENT '会话类型：0-私聊，1-群聊',
  `target_id` BIGINT NOT NULL COMMENT '目标ID：私聊为用户ID，群聊为群组ID',
  `unread_count` INT NOT NULL DEFAULT 0 COMMENT '未读消息数',
  `last_message_id` BIGINT DEFAULT NULL COMMENT '最后一条消息ID',
  `last_message_time` DATETIME DEFAULT NULL COMMENT '最后一条消息时间',
  `is_top` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶：0-否，1-是',
  `is_muted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否免打扰：0-否，1-是',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-正常，1-已删除',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_conversation` (`user_id`, `conversation_type`, `target_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_target_id` (`target_id`),
  KEY `idx_last_message_time` (`last_message_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话表';
```

### 3.8 消息表 (message)

存储所有消息记录。考虑到消息量可能非常大，可以按照时间范围分表。

```sql
CREATE TABLE `message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `conversation_type` TINYINT NOT NULL COMMENT '会话类型：0-私聊，1-群聊',
  `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
  `receiver_id` BIGINT NOT NULL COMMENT '接收者ID：私聊为用户ID，群聊为群组ID',
  `content_type` TINYINT NOT NULL COMMENT '内容类型：0-文本，1-图片，2-语音，3-视频，4-文件，5-位置，99-系统消息',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `extra` TEXT DEFAULT NULL COMMENT '附加信息，JSON格式',
  `is_recalled` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已撤回：0-否，1-是',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-正常，1-已删除',
  `send_time` DATETIME NOT NULL COMMENT '发送时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_conversation` (`conversation_type`, `sender_id`, `receiver_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_send_time` (`send_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';
```

### 3.9 消息接收状态表 (message_receipt)

记录群聊消息的接收状态，私聊消息不使用此表。

```sql
CREATE TABLE `message_receipt` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `message_id` BIGINT NOT NULL COMMENT '消息ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `read_time` DATETIME DEFAULT NULL COMMENT '已读时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_message_user` (`message_id`, `user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息接收状态表';
```

### 3.10 文件资源表 (file_resource)

存储上传的文件信息。

```sql
CREATE TABLE `file_resource` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '资源ID',
  `user_id` BIGINT NOT NULL COMMENT '上传用户ID',
  `file_name` VARCHAR(255) NOT NULL COMMENT '文件名',
  `file_type` VARCHAR(50) NOT NULL COMMENT '文件类型',
  `file_size` BIGINT NOT NULL COMMENT '文件大小(字节)',
  `file_url` VARCHAR(500) NOT NULL COMMENT '文件URL',
  `thumbnail_url` VARCHAR(500) DEFAULT NULL COMMENT '缩略图URL（图片和视频有）',
  `duration` INT DEFAULT NULL COMMENT '时长(毫秒)，音频和视频有',
  `width` INT DEFAULT NULL COMMENT '宽度(像素)，图片和视频有',
  `height` INT DEFAULT NULL COMMENT '高度(像素)，图片和视频有',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-正常，1-已删除',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件资源表';
```

## 4. Redis 数据结构设计

### 4.1 用户在线状态

```
// 用户在线状态，有效期设置为心跳超时时间的2倍
Key: "user:online:{userId}"
Value: "{deviceId}:{connectionId}"
Expiration: 300 seconds (5 minutes)
```

### 4.2 用户-设备绑定关系

```
// 用户当前登录的所有设备列表
Key: "user:devices:{userId}"
Value: Set<"{deviceId}:{connectionId}">
```

### 4.3 离线消息队列

```
// 用户离线消息ID列表，用于用户上线后批量拉取
Key: "user:offline_msg:{userId}"
Value: List<messageId>
```

### 4.4 消息已读状态

```
// 私聊消息已读状态
Key: "msg:read:private:{senderId}:{receiverId}"
Value: "{lastReadMsgId}"

// 群聊消息用户已读状态
Key: "msg:read:group:{groupId}:{userId}"
Value: "{lastReadMsgId}"
```

### 4.5 群组成员列表缓存

```
// 群组成员ID列表缓存
Key: "group:members:{groupId}"
Value: Set<userId>
```

### 4.6 用户会话列表缓存

```
// 用户会话列表缓存，按最后活跃时间排序
Key: "user:conversations:{userId}"
Value: ZSet<"{conversationType}:{targetId}", lastActiveTimestamp>
```

## 5. 数据库索引策略

1. 对查询频繁的字段创建索引，如用户ID、手机号等
2. 对于关联查询的外键字段创建索引
3. 对时间范围查询的字段创建索引，如消息发送时间
4. 对于组合查询条件，创建组合索引

## 6. 数据库扩展性考虑

### 6.1 分库分表策略

随着用户量和消息量的增长，可以考虑以下分库分表策略：

1. **用户相关表**：可按用户ID哈希分库分表
2. **消息表**：可按时间范围分表，如按月或按季度
3. **会话表**：可按用户ID哈希分表

### 6.2 读写分离

针对消息历史记录等读多写少的场景，可以实施主从复制，实现读写分离。

### 6.3 冷热数据分离

可以考虑将历史消息（如超过3个月的消息）归档到低成本存储中，提高活跃数据的访问效率。

## 7. 数据安全与隐私

1. 敏感数据加密存储
2. 定期数据备份
3. 数据访问权限控制
4. 符合相关法律法规的数据保留策略

## 8. 表关系图

```
user 1 -- * friendship
user 1 -- * friend_group
user 1 -- * friend_request
user 1 -- * chat_group (as creator)
user 1 -- * group_member
user 1 -- * conversation
user 1 -- * message (as sender)
user 1 -- * message_receipt
user 1 -- * file_resource

chat_group 1 -- * group_member
chat_group 1 -- * message (as receiver for group messages)

message 1 -- * message_receipt (for group messages)
```

## 9. 注意事项

1. 消息表随着时间增长会变得非常大，需要及时归档或分表
2. 考虑使用UUID或雪花算法生成唯一ID，便于分布式系统使用
3. 关键表添加适当的冗余字段，减少关联查询
4. 对于高频访问的数据，合理使用Redis缓存
5. 定期清理过期数据，如已删除的消息、失效的好友申请等 