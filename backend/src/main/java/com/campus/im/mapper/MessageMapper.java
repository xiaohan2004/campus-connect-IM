package com.campus.im.mapper;

import com.campus.im.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 消息Mapper接口
 */
@Mapper
public interface MessageMapper {
    
    /**
     * 添加消息
     *
     * @param message 消息
     * @return 影响行数
     */
    int insert(Message message);
    
    /**
     * 根据ID查询消息
     *
     * @param id 消息ID
     * @return 消息
     */
    Message selectById(@Param("id") Long id);
    
    /**
     * 根据ID列表批量查询消息
     *
     * @param ids 消息ID列表
     * @return 消息列表
     */
    List<Message> selectByIds(@Param("ids") List<Long> ids);
    
    /**
     * 查询私聊消息
     *
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @param limit 数量限制
     * @return 消息列表
     */
    List<Message> selectPrivateMessages(
            @Param("senderId") Long senderId,
            @Param("receiverId") Long receiverId,
            @Param("limit") Integer limit);
    
    /**
     * 查询私聊消息（按时间降序）
     *
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @param beforeMessageId 消息ID（查询该消息之前的消息）
     * @param limit 数量限制
     * @return 消息列表
     */
    List<Message> selectPrivateMessagesBeforeId(
            @Param("senderId") Long senderId,
            @Param("receiverId") Long receiverId,
            @Param("beforeMessageId") Long beforeMessageId,
            @Param("limit") Integer limit);
    
    /**
     * 查询群聊消息
     *
     * @param groupId 群组ID
     * @param limit 数量限制
     * @return 消息列表
     */
    List<Message> selectGroupMessages(
            @Param("groupId") Long groupId,
            @Param("limit") Integer limit);
    
    /**
     * 查询群聊消息（按时间降序）
     *
     * @param groupId 群组ID
     * @param beforeMessageId 消息ID（查询该消息之前的消息）
     * @param limit 数量限制
     * @return 消息列表
     */
    List<Message> selectGroupMessagesBeforeId(
            @Param("groupId") Long groupId,
            @Param("beforeMessageId") Long beforeMessageId,
            @Param("limit") Integer limit);
    
    /**
     * 更新消息状态
     *
     * @param id 消息ID
     * @param status 状态
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    
    /**
     * 更新消息撤回状态
     *
     * @param id 消息ID
     * @param isRecalled 撤回状态
     * @return 影响行数
     */
    int updateRecallStatus(@Param("id") Long id, @Param("isRecalled") Integer isRecalled);
    
    /**
     * 根据手机号查询私聊消息
     *
     * @param userPhone 用户手机号
     * @param otherPhone 对方手机号
     * @param limit 数量限制
     * @param offset 偏移量
     * @return 消息列表
     */
    List<Message> selectPrivateMessagesByPhone(
            @Param("userPhone") String userPhone,
            @Param("otherPhone") String otherPhone,
            @Param("limit") int limit,
            @Param("offset") int offset);
    
    /**
     * 查询群聊消息（带偏移量）
     *
     * @param groupId 群组ID
     * @param limit 数量限制
     * @param offset 偏移量
     * @return 消息列表
     */
    List<Message> selectGroupMessagesByOffset(
            @Param("groupId") Long groupId,
            @Param("limit") int limit,
            @Param("offset") int offset);
    
    /**
     * 更新消息已读状态
     *
     * @param id 消息ID
     * @param status 状态
     * @return 影响行数
     */
    int updateReadStatus(@Param("id") Long id, @Param("status") Integer status);
    
    /**
     * 统计用户未读消息数
     *
     * @param userPhone 用户手机号
     * @return 未读消息数
     */
    int countUnreadMessages(@Param("userPhone") String userPhone);
} 