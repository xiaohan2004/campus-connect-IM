-- 如果数据库不存在则创建
CREATE DATABASE IF NOT EXISTS campus_im;

USE campus_im;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
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

-- 好友关系表
CREATE TABLE IF NOT EXISTS `friendship` (
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

-- 好友分组表
CREATE TABLE IF NOT EXISTS `friend_group` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分组ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分组名称',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友分组表';

-- 好友申请表
CREATE TABLE IF NOT EXISTS `friend_request` (
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

-- 群组表
CREATE TABLE IF NOT EXISTS `chat_group` (
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

-- 群成员表
CREATE TABLE IF NOT EXISTS `group_member` (
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

-- 会话表
CREATE TABLE IF NOT EXISTS `conversation` (
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

-- 消息表
CREATE TABLE IF NOT EXISTS `message` (
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

-- 消息接收状态表
CREATE TABLE IF NOT EXISTS `message_receipt` (
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

-- 文件资源表
CREATE TABLE IF NOT EXISTS `file_resource` (
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