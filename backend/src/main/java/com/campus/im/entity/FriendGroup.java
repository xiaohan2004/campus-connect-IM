package com.campus.im.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 好友分组实体类
 */
@Data
public class FriendGroup {
    
    /**
     * 分组ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 分组名称
     */
    private String name;
    
    /**
     * 排序
     */
    private Integer sort;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}