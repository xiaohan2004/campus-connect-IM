package com.campus.im.service;

import com.campus.im.entity.Conversation;
import com.campus.im.entity.Message;

import java.util.List;

/**
 * 会话服务接口
 */
public interface ConversationService {
    
    /**
     * 创建或获取会话
     *
     * @param userId 用户ID
     * @param conversationType 会话类型
     * @param targetId 目标ID
     * @return 会话
     */
    Conversation getOrCreateConversation(Long userId, Integer conversationType, Long targetId);
    
    /**
     * 删除会话
     *
     * @param conversationId 会话ID
     * @return 是否成功
     */
    boolean deleteConversation(Long conversationId);
    
    /**
     * 获取会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<Conversation> getConversationList(Long userId);
    
    /**
     * 更新会话未读消息数
     *
     * @param conversationId 会话ID
     * @param count 增量值（可为负数）
     * @return 是否成功
     */
    boolean updateUnreadCount(Long conversationId, Integer count);
    
    /**
     * 重置会话未读消息数
     *
     * @param conversationId 会话ID
     * @return 是否成功
     */
    boolean resetUnreadCount(Long conversationId);
    
    /**
     * 更新会话最后一条消息
     *
     * @param conversationId 会话ID
     * @param messageId 消息ID
     * @return 是否成功
     */
    boolean updateLastMessage(Long conversationId, Long messageId);
    
    /**
     * 设置会话置顶状态
     *
     * @param conversationId 会话ID
     * @param isTop 是否置顶
     * @return 是否成功
     */
    boolean setTopStatus(Long conversationId, Integer isTop);
    
    /**
     * 设置会话免打扰状态
     *
     * @param conversationId 会话ID
     * @param isMuted 是否免打扰
     * @return 是否成功
     */
    boolean setMuteStatus(Long conversationId, Integer isMuted);
    
    /**
     * 更新会话（接收新消息）
     *
     * @param userId 用户ID
     * @param message 消息
     * @return 是否成功
     */
    boolean updateConversationForNewMessage(Long userId, Message message);
    
    /**
     * 获取用户会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<Conversation> getUserConversations(Long userId);
    
    /**
     * 获取会话详情
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 会话
     */
    Conversation getConversation(Long conversationId, Long userId);
    
    /**
     * 创建或获取私聊会话
     *
     * @param userId 用户ID
     * @param targetUserId 目标用户ID
     * @return 会话
     */
    Conversation createOrGetPrivateConversation(Long userId, Long targetUserId);
    
    /**
     * 创建或获取群聊会话
     *
     * @param userId 用户ID
     * @param groupId 群组ID
     * @return 会话
     */
    Conversation createOrGetGroupConversation(Long userId, Long groupId);
    
    /**
     * 删除会话
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteConversation(Long conversationId, Long userId);
    
    /**
     * 置顶会话
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param isTop 是否置顶
     * @return 是否成功
     */
    boolean topConversation(Long conversationId, Long userId, Boolean isTop);
    
    /**
     * 设置会话免打扰
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param isMuted 是否免打扰
     * @return 是否成功
     */
    boolean muteConversation(Long conversationId, Long userId, Boolean isMuted);
    
    /**
     * 清空会话消息
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean clearConversationMessages(Long conversationId, Long userId);
    
    /**
     * 标记会话已读
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markConversationAsRead(Long conversationId, Long userId);
} 