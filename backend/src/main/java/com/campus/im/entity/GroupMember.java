package com.campus.im.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 群成员实体类
 */
@Data
public class GroupMember {
    
    /**
     * 记录ID
     */
    private Long id;
    
    /**
     * 群组ID
     */
    private Long groupId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 群内昵称
     */
    private String nickname;
    
    /**
     * 角色：0-普通成员，1-管理员，2-群主
     */
    private Integer role;
    
    /**
     * 禁言结束时间
     */
    private LocalDateTime muteEndTime;
    
    /**
     * 加入时间
     */
    private LocalDateTime joinTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 