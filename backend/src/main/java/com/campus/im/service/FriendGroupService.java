package com.campus.im.service;

import com.campus.im.entity.FriendGroup;
import java.util.List;

/**
 * 好友分组服务接口
 */
public interface FriendGroupService {
    
    /**
     * 创建好友分组
     *
     * @param userId 用户ID
     * @param name 分组名称
     * @return 好友分组
     */
    FriendGroup createFriendGroup(Long userId, String name);
    
    /**
     * 更新好友分组
     *
     * @param groupId 分组ID
     * @param userId 用户ID
     * @param name 分组名称
     * @return 是否成功
     */
    boolean updateFriendGroup(Long groupId, Long userId, String name);
    
    /**
     * 删除好友分组
     *
     * @param groupId 分组ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteFriendGroup(Long groupId, Long userId);
    
    /**
     * 获取用户的好友分组列表
     *
     * @param userId 用户ID
     * @return 好友分组列表
     */
    List<FriendGroup> getFriendGroups(Long userId);
    
    /**
     * 获取好友分组详情
     *
     * @param groupId 分组ID
     * @param userId 用户ID
     * @return 好友分组
     */
    FriendGroup getFriendGroup(Long groupId, Long userId);
    
    /**
     * 移动好友到指定分组
     *
     * @param userId 用户ID
     * @param friendId 好友ID
     * @param groupId 分组ID
     * @return 是否成功
     */
    boolean moveFriendToGroup(Long userId, Long friendId, Long groupId);
}