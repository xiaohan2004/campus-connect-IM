package com.campus.im.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 消息接收状态实体类
 */
@Data
public class MessageReceipt {
    
    /**
     * ID
     */
    private Long id;
    
    /**
     * 消息ID
     */
    private Long messageId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 是否已读：0-未读，1-已读
     */
    private Integer isRead;
    
    /**
     * 已读时间
     */
    private LocalDateTime readTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 