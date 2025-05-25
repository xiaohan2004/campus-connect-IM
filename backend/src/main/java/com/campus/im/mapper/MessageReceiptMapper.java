package com.campus.im.mapper;

import com.campus.im.entity.MessageReceipt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 消息接收状态Mapper接口
 */
@Mapper
public interface MessageReceiptMapper {
    
    /**
     * 添加消息接收状态
     *
     * @param messageReceipt 消息接收状态
     * @return 影响行数
     */
    int insert(MessageReceipt messageReceipt);
    
    /**
     * 批量添加消息接收状态
     *
     * @param messageReceipts 消息接收状态列表
     * @return 影响行数
     */
    int batchInsert(@Param("messageReceipts") List<MessageReceipt> messageReceipts);
    
    /**
     * 更新消息已读状态
     *
     * @param messageId 消息ID
     * @param userId 用户ID
     * @param isRead 是否已读
     * @return 影响行数
     */
    int updateReadStatus(@Param("messageId") Long messageId, @Param("userId") Long userId, @Param("isRead") Integer isRead);
    
    /**
     * 批量更新消息已读状态
     *
     * @param messageIds 消息ID列表
     * @param userId 用户ID
     * @param isRead 是否已读
     * @return 影响行数
     */
    int batchUpdateReadStatus(@Param("messageIds") List<Long> messageIds, @Param("userId") Long userId, @Param("isRead") Integer isRead);
    
    /**
     * 查询消息接收状态
     *
     * @param messageId 消息ID
     * @param userId 用户ID
     * @return 消息接收状态
     */
    MessageReceipt selectByMessageIdAndUserId(@Param("messageId") Long messageId, @Param("userId") Long userId);
    
    /**
     * 查询消息的接收状态列表
     *
     * @param messageId 消息ID
     * @return 消息接收状态列表
     */
    List<MessageReceipt> selectByMessageId(@Param("messageId") Long messageId);
    
    /**
     * 查询用户的消息接收状态列表
     *
     * @param userId 用户ID
     * @return 消息接收状态列表
     */
    List<MessageReceipt> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 查询消息的已读接收者ID列表
     *
     * @param messageId 消息ID
     * @return 用户ID列表
     */
    List<Long> selectReadUserIdsByMessageId(@Param("messageId") Long messageId);
    
    /**
     * 查询消息的未读接收者ID列表
     *
     * @param messageId 消息ID
     * @return 用户ID列表
     */
    List<Long> selectUnreadUserIdsByMessageId(@Param("messageId") Long messageId);
    
    /**
     * 统计消息的已读接收者数量
     *
     * @param messageId 消息ID
     * @return 数量
     */
    int countReadByMessageId(@Param("messageId") Long messageId);
    
    /**
     * 统计消息的未读接收者数量
     *
     * @param messageId 消息ID
     * @return 数量
     */
    int countUnreadByMessageId(@Param("messageId") Long messageId);
} 