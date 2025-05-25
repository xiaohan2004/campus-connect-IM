package com.campus.im.service.impl;

import com.campus.im.entity.FriendGroup;
import com.campus.im.mapper.FriendGroupMapper;
import com.campus.im.service.FriendGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 好友分组服务实现类
 */
@Service
public class FriendGroupServiceImpl implements FriendGroupService {

    @Autowired
    private FriendGroupMapper friendGroupMapper;

    @Override
    @Transactional
    public FriendGroup createFriendGroup(Long userId, String name) {
        if (userId == null || name == null || name.trim().isEmpty()) {
            return null;
        }
        
        // 检查分组名称是否已存在
        FriendGroup existingGroup = friendGroupMapper.selectByUserIdAndName(userId, name);
        if (existingGroup != null) {
            return existingGroup;
        }
        
        // 创建新分组
        FriendGroup friendGroup = new FriendGroup();
        friendGroup.setUserId(userId);
        friendGroup.setName(name);
        friendGroup.setSort(0); // 默认排序值
        
        if (friendGroupMapper.insert(friendGroup) > 0) {
            return friendGroup;
        }
        
        return null;
    }

    @Override
    @Transactional
    public boolean updateFriendGroup(Long groupId, Long userId, String name) {
        if (groupId == null || userId == null || name == null || name.trim().isEmpty()) {
            return false;
        }
        
        // 检查分组是否存在
        FriendGroup friendGroup = friendGroupMapper.selectById(groupId);
        if (friendGroup == null || !friendGroup.getUserId().equals(userId)) {
            return false;
        }
        
        // 检查新名称是否已存在
        FriendGroup existingGroup = friendGroupMapper.selectByUserIdAndName(userId, name);
        if (existingGroup != null && !existingGroup.getId().equals(groupId)) {
            return false;
        }
        
        // 更新分组名称
        friendGroup.setName(name);
        return friendGroupMapper.update(friendGroup) > 0;
    }

    @Override
    @Transactional
    public boolean deleteFriendGroup(Long groupId, Long userId) {
        if (groupId == null || userId == null) {
            return false;
        }
        
        // 检查分组是否存在
        FriendGroup friendGroup = friendGroupMapper.selectById(groupId);
        if (friendGroup == null || !friendGroup.getUserId().equals(userId)) {
            return false;
        }
        
        // 获取默认分组
        FriendGroup defaultGroup = friendGroupMapper.selectDefaultByUserId(userId);
        if (defaultGroup == null || defaultGroup.getId().equals(groupId)) {
            return false; // 不允许删除默认分组
        }
        
        // 将该分组下的好友移动到默认分组
        // 这里可以添加将好友移动到默认分组的逻辑
        
        // 删除分组
        return friendGroupMapper.deleteById(groupId) > 0;
    }

    @Override
    public List<FriendGroup> getFriendGroups(Long userId) {
        if (userId == null) {
            return null;
        }
        
        return friendGroupMapper.selectByUserId(userId);
    }

    @Override
    public FriendGroup getFriendGroup(Long groupId, Long userId) {
        if (groupId == null || userId == null) {
            return null;
        }
        
        FriendGroup friendGroup = friendGroupMapper.selectById(groupId);
        if (friendGroup != null && friendGroup.getUserId().equals(userId)) {
            return friendGroup;
        }
        
        return null;
    }

    @Override
    @Transactional
    public boolean moveFriendToGroup(Long userId, Long friendId, Long groupId) {
        if (userId == null || friendId == null || groupId == null) {
            return false;
        }
        
        // 检查分组是否存在
        FriendGroup friendGroup = friendGroupMapper.selectById(groupId);
        if (friendGroup == null || !friendGroup.getUserId().equals(userId)) {
            return false;
        }
        
        // 更新好友所在分组
        return friendGroupMapper.updateFriendGroup(userId, friendId, groupId) > 0;
    }
} 