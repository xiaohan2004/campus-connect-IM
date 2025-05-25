package com.campus.im.mapper;

import com.campus.im.entity.ChatGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 群组Mapper接口
 */
@Mapper
public interface ChatGroupMapper {
    
    /**
     * 创建群组
     *
     * @param chatGroup 群组信息
     * @return 影响行数
     */
    int insert(ChatGroup chatGroup);
    
    /**
     * 更新群组信息
     *
     * @param chatGroup 群组信息
     * @return 影响行数
     */
    int update(ChatGroup chatGroup);
    
    /**
     * 解散群组（更新状态）
     *
     * @param groupId 群组ID
     * @param status 状态
     * @return 影响行数
     */
    int updateStatus(@Param("groupId") Long groupId, @Param("status") Integer status);
    
    /**
     * 更新群组成员数量
     *
     * @param groupId 群组ID
     * @param count 增量（可为负数）
     * @return 影响行数
     */
    int updateMemberCount(@Param("groupId") Long groupId, @Param("count") Integer count);
    
    /**
     * 根据ID查询群组
     *
     * @param groupId 群组ID
     * @return 群组信息
     */
    ChatGroup selectById(@Param("groupId") Long groupId);
    
    /**
     * 查询用户创建的群组
     *
     * @param creatorId 创建者ID
     * @return 群组列表
     */
    List<ChatGroup> selectByCreator(@Param("creatorId") Long creatorId);
    
    /**
     * 查询用户所在的群组
     *
     * @param userId 用户ID
     * @return 群组列表
     */
    List<ChatGroup> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 根据群组名称模糊查询
     *
     * @param name 群组名称
     * @return 群组列表
     */
    List<ChatGroup> selectByName(@Param("name") String name);
} 