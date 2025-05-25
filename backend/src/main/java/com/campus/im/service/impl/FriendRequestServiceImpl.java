package com.campus.im.service.impl;

import com.campus.im.common.constant.FriendConstant;
import com.campus.im.entity.FriendRequest;
import com.campus.im.mapper.FriendRequestMapper;
import com.campus.im.service.FriendRequestService;
import com.campus.im.service.FriendshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 好友申请服务实现类
 */
@Service
public class FriendRequestServiceImpl implements FriendRequestService {
    
    @Autowired
    private FriendRequestMapper friendRequestMapper;
    
    @Autowired
    private FriendshipService friendshipService;
    
    @Override
    @Transactional
    public boolean sendFriendRequest(Long fromUserId, Long toUserId, String message) {
        if (fromUserId == null || toUserId == null) {
            return false;
        }
        
        // 不能添加自己为好友
        if (fromUserId.equals(toUserId)) {
            return false;
        }
        
        // 检查是否已经是好友
        if (friendshipService.isFriend(fromUserId, toUserId)) {
            return false;
        }
        
        // 检查是否已经发送过申请
        if (hasPendingFriendRequest(fromUserId, toUserId)) {
            return false;
        }
        
        // 发送好友申请
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setFromUserId(fromUserId);
        friendRequest.setToUserId(toUserId);
        friendRequest.setMessage(message);
        friendRequest.setStatus(FriendConstant.REQUEST_STATUS_PENDING);
        
        return friendRequestMapper.insert(friendRequest) > 0;
    }
    
    @Override
    @Transactional
    public boolean acceptFriendRequest(Long requestId, String remark, Long groupId) {
        if (requestId == null) {
            return false;
        }
        
        // 获取好友申请
        FriendRequest friendRequest = getFriendRequest(requestId);
        if (friendRequest == null || friendRequest.getStatus() != FriendConstant.REQUEST_STATUS_PENDING) {
            return false;
        }
        
        // 更新申请状态
        if (friendRequestMapper.updateStatus(requestId, FriendConstant.REQUEST_STATUS_ACCEPTED) <= 0) {
            return false;
        }
        
        // 添加好友关系
        return friendshipService.addFriend(friendRequest.getToUserId(), friendRequest.getFromUserId(), remark, groupId);
    }
    
    @Override
    @Transactional
    public boolean rejectFriendRequest(Long requestId) {
        if (requestId == null) {
            return false;
        }
        
        // 获取好友申请
        FriendRequest friendRequest = getFriendRequest(requestId);
        if (friendRequest == null || friendRequest.getStatus() != FriendConstant.REQUEST_STATUS_PENDING) {
            return false;
        }
        
        // 更新申请状态
        return friendRequestMapper.updateStatus(requestId, FriendConstant.REQUEST_STATUS_REJECTED) > 0;
    }
    
    @Override
    public FriendRequest getFriendRequest(Long requestId) {
        if (requestId == null) {
            return null;
        }
        
        return friendRequestMapper.selectById(requestId);
    }
    
    @Override
    public List<FriendRequest> getSentFriendRequests(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        
        return friendRequestMapper.selectByFromUserId(userId);
    }
    
    @Override
    public List<FriendRequest> getReceivedFriendRequests(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        
        return friendRequestMapper.selectByToUserId(userId);
    }
    
    @Override
    public List<FriendRequest> getPendingFriendRequests(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        
        return friendRequestMapper.selectPendingByToUserId(userId);
    }
    
    @Override
    public boolean hasPendingFriendRequest(Long fromUserId, Long toUserId) {
        if (fromUserId == null || toUserId == null) {
            return false;
        }
        
        FriendRequest friendRequest = friendRequestMapper.selectPendingByFromUserIdAndToUserId(fromUserId, toUserId);
        return friendRequest != null;
    }
} 