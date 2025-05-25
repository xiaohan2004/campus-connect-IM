package com.campus.im.service.impl;

import com.campus.im.common.constant.ConversationConstant;
import com.campus.im.common.constant.MessageConstant;
import com.campus.im.entity.Conversation;
import com.campus.im.entity.Message;
import com.campus.im.mapper.ConversationMapper;
import com.campus.im.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 会话服务实现类
 */
@Service
public class ConversationServiceImpl implements ConversationService {
    
    @Autowired
    private ConversationMapper conversationMapper;
    
    @Override
    @Transactional
    public Conversation getOrCreateConversation(Long userId, Integer conversationType, Long targetId) {
        if (userId == null || conversationType == null || targetId == null) {
            return null;
        }
        
        // 查询会话是否存在
        Conversation conversation = conversationMapper.selectByUserIdAndTarget(userId, conversationType, targetId);
        
        // 会话不存在则创建
        if (conversation == null) {
            conversation = new Conversation();
            conversation.setUserId(userId);
            conversation.setConversationType(conversationType);
            conversation.setTargetId(targetId);
            conversation.setUnreadCount(0);
            conversation.setIsTop(ConversationConstant.IS_TOP_NO);
            conversation.setIsMuted(ConversationConstant.IS_MUTED_NO);
            conversation.setStatus(ConversationConstant.STATUS_NORMAL);
            
            if (conversationMapper.insert(conversation) <= 0) {
                return null;
            }
        } else if (conversation.getStatus() == ConversationConstant.STATUS_DELETED) {
            // 会话被删除则恢复
            conversation.setStatus(ConversationConstant.STATUS_NORMAL);
            conversation.setUnreadCount(0);
            conversation.setLastMessageId(null);
            conversation.setLastMessageTime(null);
            
            if (conversationMapper.update(conversation) <= 0) {
                return null;
            }
        }
        
        return conversation;
    }
    
    @Override
    @Transactional
    public boolean deleteConversation(Long conversationId) {
        if (conversationId == null) {
            return false;
        }
        
        return conversationMapper.delete(conversationId) > 0;
    }
    
    @Override
    public List<Conversation> getConversationList(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        
        return conversationMapper.selectByUserId(userId);
    }
    
    @Override
    @Transactional
    public boolean updateUnreadCount(Long conversationId, Integer count) {
        if (conversationId == null || count == null) {
            return false;
        }
        
        return conversationMapper.updateUnreadCount(conversationId, count) > 0;
    }
    
    @Override
    @Transactional
    public boolean resetUnreadCount(Long conversationId) {
        if (conversationId == null) {
            return false;
        }
        
        return conversationMapper.resetUnreadCount(conversationId) > 0;
    }
    
    @Override
    @Transactional
    public boolean updateLastMessage(Long conversationId, Long messageId) {
        if (conversationId == null || messageId == null) {
            return false;
        }
        
        return conversationMapper.updateLastMessage(conversationId, messageId) > 0;
    }
    
    @Override
    @Transactional
    public boolean setTopStatus(Long conversationId, Integer isTop) {
        if (conversationId == null || isTop == null) {
            return false;
        }
        
        return conversationMapper.updateTopStatus(conversationId, isTop) > 0;
    }
    
    @Override
    @Transactional
    public boolean setMuteStatus(Long conversationId, Integer isMuted) {
        if (conversationId == null || isMuted == null) {
            return false;
        }
        
        return conversationMapper.updateMuteStatus(conversationId, isMuted) > 0;
    }
    
    @Override
    @Transactional
    public boolean updateConversationForNewMessage(Long userId, Message message) {
        if (userId == null || message == null) {
            return false;
        }
        
        // 创建或获取会话
        Conversation conversation = getOrCreateConversation(
                userId, 
                message.getConversationType(), 
                message.getConversationType() == MessageConstant.CONVERSATION_TYPE_PRIVATE ? 
                        (userId.equals(message.getSenderId()) ? message.getReceiverId() : message.getSenderId()) : 
                        message.getReceiverId()
        );
        
        if (conversation == null) {
            return false;
        }
        
        // 更新最后一条消息
        if (!updateLastMessage(conversation.getId(), message.getId())) {
            return false;
        }
        
        // 如果是接收消息，且不是免打扰的会话，更新未读消息数
        if (!userId.equals(message.getSenderId()) && conversation.getIsMuted() == ConversationConstant.IS_MUTED_NO) {
            return updateUnreadCount(conversation.getId(), 1);
        }
        
        return true;
    }
    
    @Override
    public List<Conversation> getUserConversations(Long userId) {
        // 复用已有的获取会话列表方法
        return getConversationList(userId);
    }
    
    @Override
    public Conversation getConversation(Long conversationId, Long userId) {
        if (conversationId == null || userId == null) {
            return null;
        }
        
        // 获取会话并验证所有者
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            return null;
        }
        
        return conversation;
    }
    
    @Override
    @Transactional
    public Conversation createOrGetPrivateConversation(Long userId, Long targetUserId) {
        if (userId == null || targetUserId == null) {
            return null;
        }
        
        // 使用已有方法创建或获取私聊会话
        return getOrCreateConversation(userId, MessageConstant.CONVERSATION_TYPE_PRIVATE, targetUserId);
    }
    
    @Override
    @Transactional
    public Conversation createOrGetGroupConversation(Long userId, Long groupId) {
        if (userId == null || groupId == null) {
            return null;
        }
        
        // 使用已有方法创建或获取群聊会话
        return getOrCreateConversation(userId, MessageConstant.CONVERSATION_TYPE_GROUP, groupId);
    }
    
    @Override
    @Transactional
    public boolean deleteConversation(Long conversationId, Long userId) {
        if (conversationId == null || userId == null) {
            return false;
        }
        
        // 获取会话并验证所有者
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            return false;
        }
        
        // 删除会话
        return conversationMapper.delete(conversationId) > 0;
    }
    
    @Override
    @Transactional
    public boolean topConversation(Long conversationId, Long userId, Boolean isTop) {
        if (conversationId == null || userId == null || isTop == null) {
            return false;
        }
        
        // 获取会话并验证所有者
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            return false;
        }
        
        // 设置置顶状态
        int topStatus = isTop ? ConversationConstant.IS_TOP_YES : ConversationConstant.IS_TOP_NO;
        return conversationMapper.updateTopStatus(conversationId, topStatus) > 0;
    }
    
    @Override
    @Transactional
    public boolean muteConversation(Long conversationId, Long userId, Boolean isMuted) {
        if (conversationId == null || userId == null || isMuted == null) {
            return false;
        }
        
        // 获取会话并验证所有者
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            return false;
        }
        
        // 设置免打扰状态
        int muteStatus = isMuted ? ConversationConstant.IS_MUTED_YES : ConversationConstant.IS_MUTED_NO;
        return conversationMapper.updateMuteStatus(conversationId, muteStatus) > 0;
    }
    
    @Override
    @Transactional
    public boolean clearConversationMessages(Long conversationId, Long userId) {
        if (conversationId == null || userId == null) {
            return false;
        }
        
        // 获取会话并验证所有者
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            return false;
        }
        
        // 清空会话消息（重置最后一条消息和未读消息数）
        conversation.setLastMessageId(null);
        conversation.setLastMessageTime(null);
        conversation.setUnreadCount(0);
        
        return conversationMapper.update(conversation) > 0;
    }
    
    @Override
    @Transactional
    public boolean markConversationAsRead(Long conversationId, Long userId) {
        if (conversationId == null || userId == null) {
            return false;
        }
        
        // 获取会话并验证所有者
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            return false;
        }
        
        // 重置未读消息数
        return resetUnreadCount(conversationId);
    }
} 