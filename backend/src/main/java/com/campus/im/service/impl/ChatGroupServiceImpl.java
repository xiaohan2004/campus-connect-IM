package com.campus.im.service.impl;

import com.campus.im.common.constant.GroupConstant;
import com.campus.im.entity.ChatGroup;
import com.campus.im.entity.GroupMember;
import com.campus.im.entity.User;
import com.campus.im.mapper.ChatGroupMapper;
import com.campus.im.mapper.GroupMemberMapper;
import com.campus.im.service.ChatGroupService;
import com.campus.im.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 群组服务实现类
 */
@Service
public class ChatGroupServiceImpl implements ChatGroupService {
    
    @Autowired
    private ChatGroupMapper chatGroupMapper;
    
    @Autowired
    private GroupMemberMapper groupMemberMapper;
    
    @Autowired
    private UserService userService;
    
    @Override
    @Transactional
    public ChatGroup createGroup(String name, String avatar, Long creatorId, String description, String announcement, List<Long> memberIds) {
        if (name == null || name.isEmpty() || creatorId == null) {
            return null;
        }
        
        // 创建群组
        ChatGroup chatGroup = new ChatGroup();
        chatGroup.setName(name);
        chatGroup.setAvatar(avatar);
        chatGroup.setCreatorId(creatorId);
        chatGroup.setDescription(description);
        chatGroup.setAnnouncement(announcement);
        chatGroup.setMemberCount(1); // 初始只有创建者
        chatGroup.setMaxMemberCount(GroupConstant.DEFAULT_MAX_MEMBER_COUNT);
        chatGroup.setStatus(GroupConstant.STATUS_NORMAL);
        
        if (chatGroupMapper.insert(chatGroup) <= 0) {
            return null;
        }
        
        // 添加创建者为群主
        GroupMember creator = new GroupMember();
        creator.setGroupId(chatGroup.getId());
        creator.setUserId(creatorId);
        creator.setRole(GroupConstant.ROLE_OWNER);
        creator.setJoinTime(LocalDateTime.now());
        
        if (groupMemberMapper.insert(creator) <= 0) {
            return null;
        }
        
        // 添加其他成员
        if (memberIds != null && !memberIds.isEmpty()) {
            addGroupMembers(chatGroup.getId(), memberIds, creatorId);
        }
        
        return chatGroup;
    }
    
    @Override
    @Transactional
    public boolean disbandGroup(Long groupId, Long operatorId) {
        if (groupId == null || operatorId == null) {
            return false;
        }
        
        // 检查操作者是否是群主
        if (!hasPermission(groupId, operatorId, GroupConstant.ROLE_OWNER)) {
            return false;
        }
        
        // 更新群组状态为已解散
        return chatGroupMapper.updateStatus(groupId, GroupConstant.STATUS_DISSOLVED) > 0;
    }
    
    @Override
    @Transactional
    public boolean updateGroupInfo(Long groupId, String name, String avatar, String description, String announcement, Long operatorId) {
        if (groupId == null || operatorId == null) {
            return false;
        }
        
        // 检查操作者是否有权限（群主或管理员）
        if (!hasPermission(groupId, operatorId, GroupConstant.ROLE_ADMIN)) {
            return false;
        }
        
        ChatGroup chatGroup = chatGroupMapper.selectById(groupId);
        if (chatGroup == null || chatGroup.getStatus() == GroupConstant.STATUS_DISSOLVED) {
            return false;
        }
        
        // 更新群组信息
        if (name != null && !name.isEmpty()) {
            chatGroup.setName(name);
        }
        if (avatar != null) {
            chatGroup.setAvatar(avatar);
        }
        if (description != null) {
            chatGroup.setDescription(description);
        }
        if (announcement != null) {
            chatGroup.setAnnouncement(announcement);
        }
        
        return chatGroupMapper.update(chatGroup) > 0;
    }
    
    @Override
    @Transactional
    public boolean addGroupMember(Long groupId, Long userId, Long operatorId) {
        if (groupId == null || userId == null || operatorId == null) {
            return false;
        }
        
        // 检查操作者是否有权限（群主或管理员）
        if (!hasPermission(groupId, operatorId, GroupConstant.ROLE_ADMIN)) {
            return false;
        }
        
        ChatGroup chatGroup = chatGroupMapper.selectById(groupId);
        if (chatGroup == null || chatGroup.getStatus() == GroupConstant.STATUS_DISSOLVED) {
            return false;
        }
        
        // 检查用户是否已经在群组中
        if (isUserInGroup(groupId, userId)) {
            return false;
        }
        
        // 检查群组是否已满
        if (chatGroup.getMemberCount() >= chatGroup.getMaxMemberCount()) {
            return false;
        }
        
        // 添加群成员
        GroupMember groupMember = new GroupMember();
        groupMember.setGroupId(groupId);
        groupMember.setUserId(userId);
        groupMember.setRole(GroupConstant.ROLE_MEMBER);
        groupMember.setJoinTime(LocalDateTime.now());
        
        if (groupMemberMapper.insert(groupMember) <= 0) {
            return false;
        }
        
        // 更新群组成员数量
        return chatGroupMapper.updateMemberCount(groupId, 1) > 0;
    }
    
    @Override
    @Transactional
    public boolean addGroupMembers(Long groupId, List<Long> userIds, Long operatorId) {
        if (groupId == null || userIds == null || userIds.isEmpty() || operatorId == null) {
            return false;
        }
        
        // 检查操作者是否有权限（群主或管理员）
        if (!hasPermission(groupId, operatorId, GroupConstant.ROLE_ADMIN)) {
            return false;
        }
        
        ChatGroup chatGroup = chatGroupMapper.selectById(groupId);
        if (chatGroup == null || chatGroup.getStatus() == GroupConstant.STATUS_DISSOLVED) {
            return false;
        }
        
        // 过滤掉已经在群组中的用户
        List<Long> newUserIds = userIds.stream()
                .filter(userId -> !isUserInGroup(groupId, userId))
                .collect(Collectors.toList());
        
        if (newUserIds.isEmpty()) {
            return true;
        }
        
        // 检查群组是否已满
        if (chatGroup.getMemberCount() + newUserIds.size() > chatGroup.getMaxMemberCount()) {
            return false;
        }
        
        // 批量添加群成员
        List<GroupMember> groupMembers = new ArrayList<>();
        for (Long userId : newUserIds) {
            GroupMember groupMember = new GroupMember();
            groupMember.setGroupId(groupId);
            groupMember.setUserId(userId);
            groupMember.setRole(GroupConstant.ROLE_MEMBER);
            groupMember.setJoinTime(LocalDateTime.now());
            groupMembers.add(groupMember);
        }
        
        if (groupMemberMapper.batchInsert(groupMembers) <= 0) {
            return false;
        }
        
        // 更新群组成员数量
        return chatGroupMapper.updateMemberCount(groupId, newUserIds.size()) > 0;
    }
    
    @Override
    @Transactional
    public boolean removeGroupMember(Long groupId, Long userId, Long operatorId) {
        if (groupId == null || userId == null || operatorId == null) {
            return false;
        }
        
        // 检查操作者是否有权限（群主或管理员）
        if (!hasPermission(groupId, operatorId, GroupConstant.ROLE_ADMIN)) {
            return false;
        }
        
        // 群主不能被移除
        GroupMember member = groupMemberMapper.selectByGroupIdAndUserId(groupId, userId);
        if (member == null || member.getRole() == GroupConstant.ROLE_OWNER) {
            return false;
        }
        
        // 管理员不能移除其他管理员，只有群主可以
        if (member.getRole() == GroupConstant.ROLE_ADMIN) {
            GroupMember operator = groupMemberMapper.selectByGroupIdAndUserId(groupId, operatorId);
            if (operator == null || operator.getRole() != GroupConstant.ROLE_OWNER) {
                return false;
            }
        }
        
        // 移除群成员
        if (groupMemberMapper.delete(groupId, userId) <= 0) {
            return false;
        }
        
        // 更新群组成员数量
        return chatGroupMapper.updateMemberCount(groupId, -1) > 0;
    }
    
    @Override
    @Transactional
    public boolean removeGroupMembers(Long groupId, List<Long> userIds, Long operatorId) {
        if (groupId == null || userIds == null || userIds.isEmpty() || operatorId == null) {
            return false;
        }
        
        // 逐个移除成员（进行权限检查）
        boolean allSuccess = true;
        for (Long userId : userIds) {
            if (!removeGroupMember(groupId, userId, operatorId)) {
                allSuccess = false;
            }
        }
        
        return allSuccess;
    }
    
    @Override
    @Transactional
    public boolean quitGroup(Long groupId, Long userId) {
        if (groupId == null || userId == null) {
            return false;
        }
        
        // 检查用户是否在群组中
        GroupMember member = groupMemberMapper.selectByGroupIdAndUserId(groupId, userId);
        if (member == null) {
            return false;
        }
        
        // 群主不能退出群，必须先转让群主或解散群
        if (member.getRole() == GroupConstant.ROLE_OWNER) {
            return false;
        }
        
        // 退出群组
        if (groupMemberMapper.delete(groupId, userId) <= 0) {
            return false;
        }
        
        // 更新群组成员数量
        return chatGroupMapper.updateMemberCount(groupId, -1) > 0;
    }
    
    @Override
    @Transactional
    public boolean setGroupMemberRole(Long groupId, Long userId, Integer role, Long operatorId) {
        if (groupId == null || userId == null || role == null || operatorId == null) {
            return false;
        }
        
        // 只有群主可以设置管理员
        if (!hasPermission(groupId, operatorId, GroupConstant.ROLE_OWNER)) {
            return false;
        }
        
        // 不能设置自己的角色
        if (userId.equals(operatorId)) {
            return false;
        }
        
        // 检查用户是否在群组中
        if (!isUserInGroup(groupId, userId)) {
            return false;
        }
        
        // 不能设置为群主
        if (role == GroupConstant.ROLE_OWNER) {
            return false;
        }
        
        // 设置角色
        return groupMemberMapper.updateRole(groupId, userId, role) > 0;
    }
    
    @Override
    @Transactional
    public boolean setGroupMemberNickname(Long groupId, Long userId, String nickname) {
        if (groupId == null || userId == null) {
            return false;
        }
        
        // 检查用户是否在群组中
        if (!isUserInGroup(groupId, userId)) {
            return false;
        }
        
        // 设置群内昵称
        return groupMemberMapper.updateNickname(groupId, userId, nickname) > 0;
    }
    
    @Override
    @Transactional
    public boolean muteGroupMember(Long groupId, Long userId, LocalDateTime muteEndTime, Long operatorId) {
        if (groupId == null || userId == null || muteEndTime == null || operatorId == null) {
            return false;
        }
        
        // 检查操作者是否有权限（群主或管理员）
        if (!hasPermission(groupId, operatorId, GroupConstant.ROLE_ADMIN)) {
            return false;
        }
        
        // 检查被禁言用户是否在群组中
        GroupMember member = groupMemberMapper.selectByGroupIdAndUserId(groupId, userId);
        if (member == null) {
            return false;
        }
        
        // 群主不能被禁言
        if (member.getRole() == GroupConstant.ROLE_OWNER) {
            return false;
        }
        
        // 管理员不能禁言其他管理员，只有群主可以
        if (member.getRole() == GroupConstant.ROLE_ADMIN) {
            GroupMember operator = groupMemberMapper.selectByGroupIdAndUserId(groupId, operatorId);
            if (operator == null || operator.getRole() != GroupConstant.ROLE_OWNER) {
                return false;
            }
        }
        
        // 设置禁言
        return groupMemberMapper.updateMuteStatus(groupId, userId, muteEndTime) > 0;
    }
    
    @Override
    @Transactional
    public boolean unmuteGroupMember(Long groupId, Long userId, Long operatorId) {
        if (groupId == null || userId == null || operatorId == null) {
            return false;
        }
        
        // 检查操作者是否有权限（群主或管理员）
        if (!hasPermission(groupId, operatorId, GroupConstant.ROLE_ADMIN)) {
            return false;
        }
        
        // 检查用户是否在群组中
        if (!isUserInGroup(groupId, userId)) {
            return false;
        }
        
        // 取消禁言
        return groupMemberMapper.updateMuteStatus(groupId, userId, null) > 0;
    }
    
    @Override
    public ChatGroup getGroupInfo(Long groupId) {
        if (groupId == null) {
            return null;
        }
        
        return chatGroupMapper.selectById(groupId);
    }
    
    @Override
    public GroupMember getGroupMember(Long groupId, Long userId) {
        if (groupId == null || userId == null) {
            return null;
        }
        
        return groupMemberMapper.selectByGroupIdAndUserId(groupId, userId);
    }
    
    @Override
    public List<GroupMember> getGroupMembers(Long groupId) {
        if (groupId == null) {
            return Collections.emptyList();
        }
        
        return groupMemberMapper.selectByGroupId(groupId);
    }
    
    @Override
    public List<User> getGroupMemberUsers(Long groupId) {
        List<GroupMember> members = getGroupMembers(groupId);
        if (members.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<Long> userIds = members.stream()
                .map(GroupMember::getUserId)
                .collect(Collectors.toList());
        
        return userService.getUserByIds(userIds);
    }
    
    @Override
    public List<ChatGroup> getUserGroups(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        
        return chatGroupMapper.selectByUserId(userId);
    }
    
    @Override
    public List<ChatGroup> getUserCreatedGroups(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        
        return chatGroupMapper.selectByCreator(userId);
    }
    
    @Override
    public boolean isUserInGroup(Long groupId, Long userId) {
        if (groupId == null || userId == null) {
            return false;
        }
        
        GroupMember member = groupMemberMapper.selectByGroupIdAndUserId(groupId, userId);
        return member != null;
    }
    
    @Override
    public boolean hasPermission(Long groupId, Long userId, Integer requiredRole) {
        if (groupId == null || userId == null || requiredRole == null) {
            return false;
        }
        
        GroupMember member = groupMemberMapper.selectByGroupIdAndUserId(groupId, userId);
        if (member == null) {
            return false;
        }
        
        // 群主有所有权限
        if (member.getRole() == GroupConstant.ROLE_OWNER) {
            return true;
        }
        
        // 管理员有管理权限和普通成员权限
        if (member.getRole() == GroupConstant.ROLE_ADMIN && requiredRole <= GroupConstant.ROLE_ADMIN) {
            return true;
        }
        
        // 普通成员只有普通成员权限
        return member.getRole() == requiredRole;
    }
    
    @Override
    public List<ChatGroup> getAllGroups() {
        return chatGroupMapper.selectList();
    }
} 