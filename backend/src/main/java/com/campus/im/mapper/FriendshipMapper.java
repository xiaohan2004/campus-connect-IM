package com.campus.im.mapper;

import com.campus.im.entity.Friendship;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 好友关系Mapper接口
 */
@Mapper
public interface FriendshipMapper {
    
    /**
     * 添加好友关系
     *
     * @param friendship 好友关系
     * @return 影响行数
     */
    int insert(Friendship friendship);
    
    /**
     * 更新好友关系
     *
     * @param friendship 好友关系
     * @return 影响行数
     */
    int update(Friendship friendship);
    
    /**
     * 删除好友关系
     *
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return 影响行数
     */
    int delete(@Param("userId") Long userId, @Param("friendId") Long friendId);
    
    /**
     * 查询好友关系
     *
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return 好友关系
     */
    Friendship selectByUserIdAndFriendId(@Param("userId") Long userId, @Param("friendId") Long friendId);
    
    /**
     * 查询用户的好友列表
     *
     * @param userId 用户ID
     * @return 好友关系列表
     */
    List<Friendship> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 查询用户指定分组的好友列表
     *
     * @param userId 用户ID
     * @param groupId 分组ID
     * @return 好友关系列表
     */
    List<Friendship> selectByUserIdAndGroupId(@Param("userId") Long userId, @Param("groupId") Long groupId);
    
    /**
     * 更新好友备注
     *
     * @param userId 用户ID
     * @param friendId 好友ID
     * @param remark 备注
     * @return 影响行数
     */
    int updateRemark(@Param("userId") Long userId, @Param("friendId") Long friendId, @Param("remark") String remark);
    
    /**
     * 更新好友分组
     *
     * @param userId 用户ID
     * @param friendId 好友ID
     * @param groupId 分组ID
     * @return 影响行数
     */
    int updateGroup(@Param("userId") Long userId, @Param("friendId") Long friendId, @Param("groupId") Long groupId);
    
    /**
     * 更新好友状态
     *
     * @param userId 用户ID
     * @param friendId 好友ID
     * @param status 状态
     * @return 影响行数
     */
    int updateStatus(@Param("userId") Long userId, @Param("friendId") Long friendId, @Param("status") Integer status);
}