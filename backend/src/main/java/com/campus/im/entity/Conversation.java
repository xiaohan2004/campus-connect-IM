package com.campus.im.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 会话实体类
 */
@Data
public class Conversation {
    
    /**
     * 会话ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 会话类型：0-私聊，1-群聊
     */
    private Integer conversationType;
    
    /**
     * 目标ID：私聊为用户ID，群聊为群组ID
     */
    private Long targetId;
    
    /**
     * 未读消息数
     */
    private Integer unreadCount;
    
    /**
     * 最后一条消息ID
     */
    private Long lastMessageId;
    
    /**
     * 最后一条消息时间
     */
    private LocalDateTime lastMessageTime;
    
    /**
     * 是否置顶：0-否，1-是
     */
    private Integer isTop;
    
    /**
     * 是否免打扰：0-否，1-是
     */
    private Integer isMuted;
    
    /**
     * 状态：0-正常，1-已删除
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 