package com.campus.im.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 好友关系实体类
 */
@Data
public class Friendship {
    
    /**
     * 关系ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 好友ID
     */
    private Long friendId;
    
    /**
     * 好友备注
     */
    private String remark;
    
    /**
     * 好友分组ID
     */
    private Long groupId;
    
    /**
     * 状态：0-正常，1-拉黑
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