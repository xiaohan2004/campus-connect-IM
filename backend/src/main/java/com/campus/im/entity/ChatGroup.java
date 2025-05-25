package com.campus.im.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 群组实体类
 */
@Data
public class ChatGroup {
    
    /**
     * 群组ID
     */
    private Long id;
    
    /**
     * 群组名称
     */
    private String name;
    
    /**
     * 群头像URL
     */
    private String avatar;
    
    /**
     * 创建者ID
     */
    private Long creatorId;
    
    /**
     * 群组描述
     */
    private String description;
    
    /**
     * 群公告
     */
    private String announcement;
    
    /**
     * 成员数量
     */
    private Integer memberCount;
    
    /**
     * 最大成员数量
     */
    private Integer maxMemberCount;
    
    /**
     * 状态：0-正常，1-解散
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