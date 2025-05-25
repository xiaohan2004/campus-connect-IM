package com.campus.im.service;

import com.campus.im.entity.FriendRequest;

import java.util.List;

/**
 * 好友申请服务接口
 */
public interface FriendRequestService {
    
    /**
     * 发送好友申请
     *
     * @param fromUserId 申请人ID
     * @param toUserId 接收人ID
     * @param message 申请消息
     * @return 是否成功
     */
    boolean sendFriendRequest(Long fromUserId, Long toUserId, String message);
    
    /**
     * 接受好友申请
     *
     * @param requestId 申请ID
     * @param remark 好友备注
     * @param groupId 好友分组ID
     * @return 是否成功
     */
    boolean acceptFriendRequest(Long requestId, String remark, Long groupId);
    
    /**
     * 拒绝好友申请
     *
     * @param requestId 申请ID
     * @return 是否成功
     */
    boolean rejectFriendRequest(Long requestId);
    
    /**
     * 获取好友申请详情
     *
     * @param requestId 申请ID
     * @return 好友申请
     */
    FriendRequest getFriendRequest(Long requestId);
    
    /**
     * 获取发送的好友申请列表
     *
     * @param userId 用户ID
     * @return 好友申请列表
     */
    List<FriendRequest> getSentFriendRequests(Long userId);
    
    /**
     * 获取收到的好友申请列表
     *
     * @param userId 用户ID
     * @return 好友申请列表
     */
    List<FriendRequest> getReceivedFriendRequests(Long userId);
    
    /**
     * 获取待处理的好友申请列表
     *
     * @param userId 用户ID
     * @return 好友申请列表
     */
    List<FriendRequest> getPendingFriendRequests(Long userId);
    
    /**
     * 检查是否存在未处理的好友申请
     *
     * @param fromUserId 申请人ID
     * @param toUserId 接收人ID
     * @return 是否存在
     */
    boolean hasPendingFriendRequest(Long fromUserId, Long toUserId);
} 