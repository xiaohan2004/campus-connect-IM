package com.campus.im.service;

import com.campus.im.entity.MessageReceipt;

import java.util.List;

/**
 * 消息接收状态服务接口
 */
public interface MessageReceiptService {
    
    /**
     * 添加消息接收状态
     *
     * @param messageId 消息ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean addMessageReceipt(Long messageId, Long userId);
    
    /**
     * 批量添加消息接收状态
     *
     * @param messageId 消息ID
     * @param userIds 用户ID列表
     * @return 是否成功
     */
    boolean addMessageReceipts(Long messageId, List<Long> userIds);
    
    /**
     * 更新消息已读状态
     *
     * @param messageId 消息ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markAsRead(Long messageId, Long userId);
    
    /**
     * 批量更新消息已读状态
     *
     * @param messageIds 消息ID列表
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markAsRead(List<Long> messageIds, Long userId);
    
    /**
     * 获取消息接收状态
     *
     * @param messageId 消息ID
     * @param userId 用户ID
     * @return 消息接收状态
     */
    MessageReceipt getMessageReceipt(Long messageId, Long userId);
    
    /**
     * 获取消息的接收状态列表
     *
     * @param messageId 消息ID
     * @return 消息接收状态列表
     */
    List<MessageReceipt> getMessageReceipts(Long messageId);
    
    /**
     * 获取消息的已读用户ID列表
     *
     * @param messageId 消息ID
     * @return 用户ID列表
     */
    List<Long> getReadUserIds(Long messageId);
    
    /**
     * 获取消息的未读用户ID列表
     *
     * @param messageId 消息ID
     * @return 用户ID列表
     */
    List<Long> getUnreadUserIds(Long messageId);
    
    /**
     * 统计消息已读数量
     *
     * @param messageId 消息ID
     * @return 数量
     */
    int countRead(Long messageId);
    
    /**
     * 统计消息未读数量
     *
     * @param messageId 消息ID
     * @return 数量
     */
    int countUnread(Long messageId);
} 