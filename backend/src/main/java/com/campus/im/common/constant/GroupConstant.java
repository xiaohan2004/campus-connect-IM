package com.campus.im.common.constant;

/**
 * 群聊相关常量
 */
public class GroupConstant {
    
    /**
     * 群组状态：正常
     */
    public static final int STATUS_NORMAL = 0;
    
    /**
     * 群组状态：已解散
     */
    public static final int STATUS_DISSOLVED = 1;
    
    /**
     * 成员角色：普通成员
     */
    public static final int ROLE_MEMBER = 0;
    
    /**
     * 成员角色：管理员
     */
    public static final int ROLE_ADMIN = 1;
    
    /**
     * 成员角色：群主
     */
    public static final int ROLE_OWNER = 2;
    
    /**
     * 默认最大成员数
     */
    public static final int DEFAULT_MAX_MEMBER_COUNT = 200;
} 