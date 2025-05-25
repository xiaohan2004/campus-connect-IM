package com.campus.im.mapper;

import com.campus.im.entity.GroupMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 群成员Mapper接口
 */
@Mapper
public interface GroupMemberMapper {
    
    /**
     * 添加群成员
     *
     * @param groupMember 群成员信息
     * @return 影响行数
     */
    int insert(GroupMember groupMember);
    
    /**
     * 更新群成员信息
     *
     * @param groupMember 群成员信息
     * @return 影响行数
     */
    int update(GroupMember groupMember);
    
    /**
     * 移除群成员
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 影响行数
     */
    int delete(@Param("groupId") Long groupId, @Param("userId") Long userId);
    
    /**
     * 查询群成员
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 群成员信息
     */
    GroupMember selectByGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") Long userId);
    
    /**
     * 查询群组的所有成员
     *
     * @param groupId 群组ID
     * @return 群成员列表
     */
    List<GroupMember> selectByGroupId(@Param("groupId") Long groupId);
    
    /**
     * 查询用户加入的所有群组
     *
     * @param userId 用户ID
     * @return 群成员列表
     */
    List<GroupMember> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 查询群组中特定角色的成员
     *
     * @param groupId 群组ID
     * @param role 角色
     * @return 群成员列表
     */
    List<GroupMember> selectByGroupIdAndRole(@Param("groupId") Long groupId, @Param("role") Integer role);
    
    /**
     * 更新群成员角色
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param role 角色
     * @return 影响行数
     */
    int updateRole(@Param("groupId") Long groupId, @Param("userId") Long userId, @Param("role") Integer role);
    
    /**
     * 更新群成员昵称
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param nickname 昵称
     * @return 影响行数
     */
    int updateNickname(@Param("groupId") Long groupId, @Param("userId") Long userId, @Param("nickname") String nickname);
    
    /**
     * 更新群成员禁言状态
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param muteEndTime 禁言结束时间
     * @return 影响行数
     */
    int updateMuteStatus(@Param("groupId") Long groupId, @Param("userId") Long userId, @Param("muteEndTime") LocalDateTime muteEndTime);
    
    /**
     * 批量添加群成员
     *
     * @param groupMembers 群成员列表
     * @return 影响行数
     */
    int batchInsert(@Param("groupMembers") List<GroupMember> groupMembers);
    
    /**
     * 批量移除群成员
     *
     * @param groupId 群组ID
     * @param userIds 用户ID列表
     * @return 影响行数
     */
    int batchDelete(@Param("groupId") Long groupId, @Param("userIds") List<Long> userIds);
} 