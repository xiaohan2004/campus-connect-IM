package com.campus.im.service.impl;

import com.campus.im.common.constant.FriendConstant;
import com.campus.im.entity.Friendship;
import com.campus.im.entity.User;
import com.campus.im.mapper.FriendshipMapper;
import com.campus.im.service.FriendshipService;
import com.campus.im.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 好友关系服务实现类
 */
@Service
public class FriendshipServiceImpl implements FriendshipService {
    
    @Autowired
    private FriendshipMapper friendshipMapper;
    
    @Autowired
    private UserService userService;
    
    @Override
    @Transactional
    public boolean addFriend(Long userId, Long friendId, String remark, Long groupId) {
        if (userId == null || friendId == null) {
            return false;
        }
        
        // 检查是否已经是好友
        Friendship existFriendship = friendshipMapper.selectByUserIdAndFriendId(userId, friendId);
        if (existFriendship != null) {
            return false;
        }
        
        // 添加好友关系（双向）
        Friendship friendship1 = new Friendship();
        friendship1.setUserId(userId);
        friendship1.setFriendId(friendId);
        friendship1.setRemark(remark);
        friendship1.setGroupId(groupId);
        friendship1.setStatus(FriendConstant.STATUS_NORMAL);
        
        Friendship friendship2 = new Friendship();
        friendship2.setUserId(friendId);
        friendship2.setFriendId(userId);
        friendship2.setStatus(FriendConstant.STATUS_NORMAL);
        
        return friendshipMapper.insert(friendship1) > 0 && friendshipMapper.insert(friendship2) > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteFriend(Long userId, Long friendId) {
        if (userId == null || friendId == null) {
            return false;
        }
        
        // 删除好友关系（双向）
        return friendshipMapper.delete(userId, friendId) > 0 && friendshipMapper.delete(friendId, userId) > 0;
    }
    
    @Override
    public boolean updateFriendRemark(Long userId, Long friendId, String remark) {
        if (userId == null || friendId == null) {
            return false;
        }
        
        return friendshipMapper.updateRemark(userId, friendId, remark) > 0;
    }
    
    @Override
    public boolean updateFriendGroup(Long userId, Long friendId, Long groupId) {
        if (userId == null || friendId == null) {
            return false;
        }
        
        return friendshipMapper.updateGroup(userId, friendId, groupId) > 0;
    }
    
    @Override
    public boolean updateFriendStatus(Long userId, Long friendId, Integer status) {
        if (userId == null || friendId == null || status == null) {
            return false;
        }
        
        return friendshipMapper.updateStatus(userId, friendId, status) > 0;
    }
    
    @Override
    public Friendship getFriendship(Long userId, Long friendId) {
        if (userId == null || friendId == null) {
            return null;
        }
        
        return friendshipMapper.selectByUserIdAndFriendId(userId, friendId);
    }
    
    @Override
    public List<Friendship> getFriendList(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        
        return friendshipMapper.selectByUserId(userId);
    }
    
    @Override
    public List<Long> getFriendIdList(Long userId) {
        List<Friendship> friendships = getFriendList(userId);
        if (friendships.isEmpty()) {
            return Collections.emptyList();
        }
        
        return friendships.stream()
                .map(Friendship::getFriendId)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<User> getFriendUserList(Long userId) {
        List<Long> friendIds = getFriendIdList(userId);
        if (friendIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        return userService.getUserByIds(friendIds);
    }
    
    @Override
    public List<Friendship> getFriendListByGroupId(Long userId, Long groupId) {
        if (userId == null || groupId == null) {
            return Collections.emptyList();
        }
        
        return friendshipMapper.selectByUserIdAndGroupId(userId, groupId);
    }
    
    @Override
    public boolean isFriend(Long userId, Long friendId) {
        if (userId == null || friendId == null) {
            return false;
        }
        
        Friendship friendship = friendshipMapper.selectByUserIdAndFriendId(userId, friendId);
        return friendship != null && friendship.getStatus() == FriendConstant.STATUS_NORMAL;
    }
} 