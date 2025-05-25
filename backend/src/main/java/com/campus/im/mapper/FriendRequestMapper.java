package com.campus.im.mapper;

import com.campus.im.entity.FriendRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 好友申请Mapper接口
 */
@Mapper
public interface FriendRequestMapper {
    
    /**
     * 添加好友申请
     *
     * @param friendRequest 好友申请
     * @return 影响行数
     */
    int insert(FriendRequest friendRequest);
    
    /**
     * 更新好友申请状态
     *
     * @param id 申请ID
     * @param status 状态
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    
    /**
     * 根据ID查询好友申请
     *
     * @param id 申请ID
     * @return 好友申请
     */
    FriendRequest selectById(@Param("id") Long id);
    
    /**
     * 查询用户发送的好友申请
     *
     * @param fromUserId 申请人ID
     * @return 好友申请列表
     */
    List<FriendRequest> selectByFromUserId(@Param("fromUserId") Long fromUserId);
    
    /**
     * 查询用户收到的好友申请
     *
     * @param toUserId 接收人ID
     * @return 好友申请列表
     */
    List<FriendRequest> selectByToUserId(@Param("toUserId") Long toUserId);
    
    /**
     * 查询用户收到的未处理好友申请
     *
     * @param toUserId 接收人ID
     * @return 好友申请列表
     */
    List<FriendRequest> selectPendingByToUserId(@Param("toUserId") Long toUserId);
    
    /**
     * 检查是否存在未处理的好友申请
     *
     * @param fromUserId 申请人ID
     * @param toUserId 接收人ID
     * @return 好友申请
     */
    FriendRequest selectPendingByFromUserIdAndToUserId(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId);
} 