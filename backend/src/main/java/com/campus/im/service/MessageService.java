package com.campus.im.service;

import com.campus.im.entity.Message;

import java.util.List;

/**
 * 消息服务接口
 */
public interface MessageService {
    
    /**
     * 发送私聊消息
     *
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @param contentType 内容类型
     * @param content 消息内容
     * @param extra 附加信息
     * @return 消息实体
     */
    Message sendPrivateMessage(Long senderId, Long receiverId, Integer contentType, String content, String extra);
    
    /**
     * 发送群聊消息
     *
     * @param senderId 发送者ID
     * @param groupId 群组ID
     * @param contentType 内容类型
     * @param content 消息内容
     * @param extra 附加信息
     * @return 消息实体
     */
    Message sendGroupMessage(Long senderId, Long groupId, Integer contentType, String content, String extra);
    
    /**
     * 撤回消息
     *
     * @param messageId 消息ID
     * @param operatorId 操作者ID
     * @return 是否成功
     */
    boolean recallMessage(Long messageId, Long operatorId);
    
    /**
     * 删除消息（逻辑删除）
     *
     * @param messageId 消息ID
     * @return 是否成功
     */
    boolean deleteMessage(Long messageId);
    
    /**
     * 获取消息详情
     *
     * @param messageId 消息ID
     * @return 消息实体
     */
    Message getMessage(Long messageId);
    
    /**
     * 获取私聊历史消息
     *
     * @param userId 用户ID
     * @param friendId 好友ID
     * @param limit 数量限制
     * @return 消息列表
     */
    List<Message> getPrivateMessageHistory(Long userId, Long friendId, Integer limit);
    
    /**
     * 获取私聊历史消息（按消息ID分页）
     *
     * @param userId 用户ID
     * @param friendId 好友ID
     * @param beforeMessageId 消息ID（查询该消息之前的消息）
     * @param limit 数量限制
     * @return 消息列表
     */
    List<Message> getPrivateMessageHistoryBeforeId(Long userId, Long friendId, Long beforeMessageId, Integer limit);
    
    /**
     * 获取群聊历史消息
     *
     * @param groupId 群组ID
     * @param limit 数量限制
     * @return 消息列表
     */
    List<Message> getGroupMessageHistory(Long groupId, Integer limit);
    
    /**
     * 获取群聊历史消息（按消息ID分页）
     *
     * @param groupId 群组ID
     * @param beforeMessageId 消息ID（查询该消息之前的消息）
     * @param limit 数量限制
     * @return 消息列表
     */
    List<Message> getGroupMessageHistoryBeforeId(Long groupId, Long beforeMessageId, Integer limit);
    
    /**
     * 保存消息（基于ChatMessage）
     *
     * @param message 聊天消息对象
     * @return 是否成功
     */
    boolean saveMessage(Message message);
    
    /**
     * 获取私聊消息列表（基于手机号）
     *
     * @param userPhone 用户手机号
     * @param otherPhone 对方手机号
     * @param limit 消息数量限制
     * @param offset 偏移量
     * @return 消息列表
     */
    List<Message> getPrivateMessages(String userPhone, String otherPhone, int limit, int offset);
    
    /**
     * 获取群聊消息列表（基于手机号）
     *
     * @param groupId 群组ID
     * @param limit 消息数量限制
     * @param offset 偏移量
     * @return 消息列表
     */
    List<Message> getGroupMessages(Long groupId, int limit, int offset);
    
    /**
     * 标记消息为已读
     *
     * @param messageId 消息ID
     * @param readerPhone 读取者手机号
     * @return 是否成功
     */
    boolean markMessageAsRead(Long messageId, String readerPhone);
    
    /**
     * 撤回消息（基于手机号）
     *
     * @param messageId 消息ID
     * @param operatorPhone 操作者手机号
     * @return 是否成功
     */
    boolean recallMessage(Long messageId, String operatorPhone);
    
    /**
     * 获取用户未读消息数
     *
     * @param userPhone 用户手机号
     * @return 未读消息数
     */
    int getUnreadMessageCount(String userPhone);
    
    /**
     * 删除消息（基于手机号）
     *
     * @param messageId 消息ID
     * @param operatorPhone 操作者手机号
     * @return 是否成功
     */
    boolean deleteMessage(Long messageId, String operatorPhone);
} 