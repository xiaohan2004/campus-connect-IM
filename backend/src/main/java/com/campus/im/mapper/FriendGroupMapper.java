package com.campus.im.mapper;

import com.campus.im.entity.FriendGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 好友分组Mapper接口
 */
@Mapper
public interface FriendGroupMapper {
    
    /**
     * 添加好友分组
     *
     * @param friendGroup 好友分组
     * @return 影响行数
     */
    int insert(FriendGroup friendGroup);
    
    /**
     * 更新好友分组
     *
     * @param friendGroup 好友分组
     * @return 影响行数
     */
    int update(FriendGroup friendGroup);
    
    /**
     * 删除好友分组
     *
     * @param id 分组ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 根据ID查询好友分组
     *
     * @param id 分组ID
     * @return 好友分组
     */
    FriendGroup selectById(@Param("id") Long id);
    
    /**
     * 根据用户ID查询好友分组列表
     *
     * @param userId 用户ID
     * @return 好友分组列表
     */
    List<FriendGroup> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID和分组名称查询好友分组
     *
     * @param userId 用户ID
     * @param name 分组名称
     * @return 好友分组
     */
    FriendGroup selectByUserIdAndName(@Param("userId") Long userId, @Param("name") String name);
    
    /**
     * 查询用户的默认好友分组
     *
     * @param userId 用户ID
     * @return 默认好友分组
     */
    FriendGroup selectDefaultByUserId(@Param("userId") Long userId);
    
    /**
     * 更新好友所在分组
     *
     * @param userId 用户ID
     * @param friendId 好友ID
     * @param groupId 分组ID
     * @return 影响行数
     */
    int updateFriendGroup(@Param("userId") Long userId, @Param("friendId") Long friendId, @Param("groupId") Long groupId);
} 