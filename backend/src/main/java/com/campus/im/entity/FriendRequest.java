package com.campus.im.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 好友申请实体类
 */
@Data
public class FriendRequest {
    
    /**
     * 申请ID
     */
    private Long id;
    
    /**
     * 申请人ID
     */
    private Long fromUserId;
    
    /**
     * 接收人ID
     */
    private Long toUserId;
    
    /**
     * 申请消息
     */
    private String message;
    
    /**
     * 状态：0-待处理，1-已接受，2-已拒绝
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