package com.campus.im.service;

import com.campus.im.entity.Friendship;
import com.campus.im.entity.User;

import java.util.List;

/**
 * 好友关系服务接口
 */
public interface FriendshipService {
    
    /**
     * 添加好友关系
     *
     * @param userId 用户ID
     * @param friendId 好友ID
     * @param remark 备注
     * @param groupId 分组ID
     * @return 是否成功
     */
    boolean addFriend(Long userId, Long friendId, String remark, Long groupId);
    
    /**
     * 删除好友关系
     *
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return 是否成功
     */
    boolean deleteFriend(Long userId, Long friendId);
    
    /**
     * 更新好友备注
     *
     * @param userId 用户ID
     * @param friendId 好友ID
     * @param remark 备注
     * @return 是否成功
     */
    boolean updateFriendRemark(Long userId, Long friendId, String remark);
    
    /**
     * 更新好友分组
     *
     * @param userId 用户ID
     * @param friendId 好友ID
     * @param groupId 分组ID
     * @return 是否成功
     */
    boolean updateFriendGroup(Long userId, Long friendId, Long groupId);
    
    /**
     * 更新好友状态（拉黑/取消拉黑）
     *
     * @param userId 用户ID
     * @param friendId 好友ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateFriendStatus(Long userId, Long friendId, Integer status);
    
    /**
     * 查询好友关系
     *
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return 好友关系
     */
    Friendship getFriendship(Long userId, Long friendId);
    
    /**
     * 获取用户的好友列表
     *
     * @param userId 用户ID
     * @return 好友关系列表
     */
    List<Friendship> getFriendList(Long userId);
    
    /**
     * 获取用户的好友ID列表
     *
     * @param userId 用户ID
     * @return 好友ID列表
     */
    List<Long> getFriendIdList(Long userId);
    
    /**
     * 获取用户好友信息列表
     *
     * @param userId 用户ID
     * @return 好友用户信息列表
     */
    List<User> getFriendUserList(Long userId);
    
    /**
     * 获取指定分组的好友列表
     *
     * @param userId 用户ID
     * @param groupId 分组ID
     * @return 好友关系列表
     */
    List<Friendship> getFriendListByGroupId(Long userId, Long groupId);
    
    /**
     * 检查是否为好友关系
     *
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return 是否为好友
     */
    boolean isFriend(Long userId, Long friendId);
} 