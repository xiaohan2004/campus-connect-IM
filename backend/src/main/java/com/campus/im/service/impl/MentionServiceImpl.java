package com.campus.im.service.impl;

import com.campus.im.service.MentionService;
import com.campus.im.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @功能服务实现类
 */
@Service
public class MentionServiceImpl implements MentionService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private UserService userService;
    
    // Redis key前缀
    private static final String GROUP_MENTION_KEY_PREFIX = "mention:group:";
    private static final String USER_MENTION_KEY_PREFIX = "mention:user:";
    private static final String UNREAD_MENTION_KEY_PREFIX = "mention:unread:";
    
    @Override
    public boolean processMentions(Long messageId, Long groupId, String content, List<Long> mentionedUserIds) {
        if (messageId == null || groupId == null || mentionedUserIds == null || mentionedUserIds.isEmpty()) {
            return false;
        }
        
        try {
            // 为每个被@的用户记录消息
            for (Long userId : mentionedUserIds) {
                // 用户在群组中的@记录
                String groupMentionKey = GROUP_MENTION_KEY_PREFIX + groupId + ":" + userId;
                redisTemplate.opsForList().leftPush(groupMentionKey, messageId);
                
                // 用户的所有@记录
                String userMentionKey = USER_MENTION_KEY_PREFIX + userId;
                Map<String, Object> mentionInfo = new HashMap<>();
                mentionInfo.put("messageId", messageId);
                mentionInfo.put("groupId", groupId);
                redisTemplate.opsForList().leftPush(userMentionKey, mentionInfo);
                
                // 未读@消息计数
                String unreadMentionKey = UNREAD_MENTION_KEY_PREFIX + userId;
                redisTemplate.opsForValue().increment(unreadMentionKey);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public List<Long> getUserMentionedMessages(Long userId, Long groupId, Integer limit, Integer offset) {
        if (userId == null || groupId == null) {
            return Collections.emptyList();
        }
        
        if (limit == null || limit <= 0) {
            limit = 20; // 默认获取20条
        }
        
        if (offset == null || offset < 0) {
            offset = 0;
        }
        
        // 获取用户在群组中被@的消息
        String groupMentionKey = GROUP_MENTION_KEY_PREFIX + groupId + ":" + userId;
        List<Object> mentions = redisTemplate.opsForList().range(groupMentionKey, offset, offset + limit - 1);
        
        if (mentions == null || mentions.isEmpty()) {
            return Collections.emptyList();
        }
        
        return mentions.stream()
                .map(mention -> Long.valueOf(mention.toString()))
                .collect(Collectors.toList());
    }
    
    @Override
    public Map<Long, Long> getAllUserMentionedMessages(Long userId, Integer limit, Integer offset) {
        if (userId == null) {
            return Collections.emptyMap();
        }
        
        if (limit == null || limit <= 0) {
            limit = 20; // 默认获取20条
        }
        
        if (offset == null || offset < 0) {
            offset = 0;
        }
        
        // 获取用户在所有群组中被@的消息
        String userMentionKey = USER_MENTION_KEY_PREFIX + userId;
        List<Object> mentions = redisTemplate.opsForList().range(userMentionKey, offset, offset + limit - 1);
        
        if (mentions == null || mentions.isEmpty()) {
            return Collections.emptyMap();
        }
        
        Map<Long, Long> result = new HashMap<>();
        for (Object mention : mentions) {
            @SuppressWarnings("unchecked")
            Map<String, Object> mentionInfo = (Map<String, Object>) mention;
            Long messageId = Long.valueOf(mentionInfo.get("messageId").toString());
            Long groupId = Long.valueOf(mentionInfo.get("groupId").toString());
            result.put(messageId, groupId);
        }
        
        return result;
    }
    
    @Override
    public boolean markMentionAsRead(Long userId, Long messageId) {
        if (userId == null || messageId == null) {
            return false;
        }
        
        try {
            // 更新未读@消息计数
            String unreadMentionKey = UNREAD_MENTION_KEY_PREFIX + userId;
            Long count = redisTemplate.opsForValue().get(unreadMentionKey) != null ? 
                    Long.valueOf(redisTemplate.opsForValue().get(unreadMentionKey).toString()) : 0L;
            
            if (count > 0) {
                redisTemplate.opsForValue().set(unreadMentionKey, count - 1);
            }
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public int getUnreadMentionCount(Long userId) {
        if (userId == null) {
            return 0;
        }
        
        // 获取未读@消息计数
        String unreadMentionKey = UNREAD_MENTION_KEY_PREFIX + userId;
        Object count = redisTemplate.opsForValue().get(unreadMentionKey);
        
        return count != null ? Integer.valueOf(count.toString()) : 0;
    }
} 