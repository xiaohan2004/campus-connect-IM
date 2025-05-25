package com.campus.im.service;

import java.util.List;
import java.util.Map;

/**
 * @功能服务接口
 * 处理群聊中的@功能
 */
public interface MentionService {
    
    /**
     * 处理消息中的@提及
     *
     * @param messageId 消息ID
     * @param groupId 群组ID
     * @param content 消息内容
     * @param mentionedUserIds 被@的用户ID列表
     * @return 是否处理成功
     */
    boolean processMentions(Long messageId, Long groupId, String content, List<Long> mentionedUserIds);
    
    /**
     * 获取用户在群组中被@的消息
     *
     * @param userId 用户ID
     * @param groupId 群组ID
     * @param limit 消息数量限制
     * @param offset 偏移量
     * @return 消息ID列表
     */
    List<Long> getUserMentionedMessages(Long userId, Long groupId, Integer limit, Integer offset);
    
    /**
     * 获取用户在所有群组中被@的消息
     *
     * @param userId 用户ID
     * @param limit 消息数量限制
     * @param offset 偏移量
     * @return 消息ID和群组ID的映射
     */
    Map<Long, Long> getAllUserMentionedMessages(Long userId, Integer limit, Integer offset);
    
    /**
     * 标记@消息为已读
     *
     * @param userId 用户ID
     * @param messageId 消息ID
     * @return 是否成功
     */
    boolean markMentionAsRead(Long userId, Long messageId);
    
    /**
     * 获取用户未读的@消息数量
     *
     * @param userId 用户ID
     * @return 未读@消息数量
     */
    int getUnreadMentionCount(Long userId);
} 