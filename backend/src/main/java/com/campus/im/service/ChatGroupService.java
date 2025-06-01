package com.campus.im.service;

import com.campus.im.entity.ChatGroup;
import com.campus.im.entity.GroupMember;
import com.campus.im.entity.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 群组服务接口
 */
public interface ChatGroupService {
    
    /**
     * 创建群组
     *
     * @param name 群组名称
     * @param avatar 群头像
     * @param creatorId 创建者ID
     * @param description 群组描述
     * @param announcement 群公告
     * @param memberIds 成员ID列表（不包括创建者）
     * @return 群组信息
     */
    ChatGroup createGroup(String name, String avatar, Long creatorId, String description, String announcement, List<Long> memberIds);
    
    /**
     * 解散群组
     *
     * @param groupId 群组ID
     * @param operatorId 操作者ID
     * @return 是否成功
     */
    boolean disbandGroup(Long groupId, Long operatorId);
    
    /**
     * 更新群组信息
     *
     * @param groupId 群组ID
     * @param name 群组名称
     * @param avatar 群头像
     * @param description 群组描述
     * @param announcement 群公告
     * @param operatorId 操作者ID
     * @return 是否成功
     */
    boolean updateGroupInfo(Long groupId, String name, String avatar, String description, String announcement, Long operatorId);
    
    /**
     * 添加群成员
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param operatorId 操作者ID
     * @return 是否成功
     */
    boolean addGroupMember(Long groupId, Long userId, Long operatorId);
    
    /**
     * 批量添加群成员
     *
     * @param groupId 群组ID
     * @param userIds 用户ID列表
     * @param operatorId 操作者ID
     * @return 是否成功
     */
    boolean addGroupMembers(Long groupId, List<Long> userIds, Long operatorId);
    
    /**
     * 移除群成员
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param operatorId 操作者ID
     * @return 是否成功
     */
    boolean removeGroupMember(Long groupId, Long userId, Long operatorId);
    
    /**
     * 批量移除群成员
     *
     * @param groupId 群组ID
     * @param userIds 用户ID列表
     * @param operatorId 操作者ID
     * @return 是否成功
     */
    boolean removeGroupMembers(Long groupId, List<Long> userIds, Long operatorId);
    
    /**
     * 退出群组
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean quitGroup(Long groupId, Long userId);
    
    /**
     * 设置群成员角色
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param role 角色
     * @param operatorId 操作者ID
     * @return 是否成功
     */
    boolean setGroupMemberRole(Long groupId, Long userId, Integer role, Long operatorId);
    
    /**
     * 设置群成员昵称
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param nickname 昵称
     * @return 是否成功
     */
    boolean setGroupMemberNickname(Long groupId, Long userId, String nickname);
    
    /**
     * 禁言群成员
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param muteEndTime 禁言结束时间
     * @param operatorId 操作者ID
     * @return 是否成功
     */
    boolean muteGroupMember(Long groupId, Long userId, LocalDateTime muteEndTime, Long operatorId);
    
    /**
     * 取消禁言
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param operatorId 操作者ID
     * @return 是否成功
     */
    boolean unmuteGroupMember(Long groupId, Long userId, Long operatorId);
    
    /**
     * 获取群组信息
     *
     * @param groupId 群组ID
     * @return 群组信息
     */
    ChatGroup getGroupInfo(Long groupId);
    
    /**
     * 获取群成员信息
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 群成员信息
     */
    GroupMember getGroupMember(Long groupId, Long userId);
    
    /**
     * 获取群成员列表
     *
     * @param groupId 群组ID
     * @return 群成员列表
     */
    List<GroupMember> getGroupMembers(Long groupId);
    
    /**
     * 获取群成员用户信息列表
     *
     * @param groupId 群组ID
     * @return 用户信息列表
     */
    List<User> getGroupMemberUsers(Long groupId);
    
    /**
     * 获取用户的群组列表
     *
     * @param userId 用户ID
     * @return 群组列表
     */
    List<ChatGroup> getUserGroups(Long userId);
    
    /**
     * 获取用户创建的群组列表
     *
     * @param userId 用户ID
     * @return 群组列表
     */
    List<ChatGroup> getUserCreatedGroups(Long userId);
    
    /**
     * 检查用户是否在群组中
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 是否在群组中
     */
    boolean isUserInGroup(Long groupId, Long userId);
    
    /**
     * 检查用户是否有权限执行操作
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param requiredRole 所需角色
     * @return 是否有权限
     */
    boolean hasPermission(Long groupId, Long userId, Integer requiredRole);
    
    /**
     * 获取所有群组列表
     *
     * @return 所有群组列表
     */
    List<ChatGroup> getAllGroups();
} 