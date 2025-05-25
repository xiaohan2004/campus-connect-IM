package com.campus.im.service.impl;

import com.campus.im.common.constant.GroupConstant;
import com.campus.im.common.constant.MessageConstant;
import com.campus.im.entity.GroupMember;
import com.campus.im.entity.Message;
import com.campus.im.mapper.GroupMemberMapper;
import com.campus.im.mapper.MessageMapper;
import com.campus.im.service.ChatGroupService;
import com.campus.im.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 消息服务实现类
 */
@Service
public class MessageServiceImpl implements MessageService {
    
    @Autowired
    private MessageMapper messageMapper;
    
    @Autowired
    private GroupMemberMapper groupMemberMapper;
    
    @Autowired
    private ChatGroupService chatGroupService;
    
    @Override
    @Transactional
    public Message sendPrivateMessage(Long senderId, Long receiverId, Integer contentType, String content, String extra) {
        if (senderId == null || receiverId == null || contentType == null || content == null) {
            return null;
        }
        
        Message message = new Message();
        message.setConversationType(MessageConstant.CONVERSATION_TYPE_PRIVATE);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContentType(contentType);
        message.setContent(content);
        message.setExtra(extra);
        message.setIsRecalled(MessageConstant.IS_RECALLED_NO);
        message.setStatus(MessageConstant.STATUS_NORMAL);
        message.setSendTime(LocalDateTime.now());
        
        if (messageMapper.insert(message) > 0) {
            return message;
        }
        
        return null;
    }
    
    @Override
    @Transactional
    public Message sendGroupMessage(Long senderId, Long groupId, Integer contentType, String content, String extra) {
        if (senderId == null || groupId == null || contentType == null || content == null) {
            return null;
        }
        
        Message message = new Message();
        message.setConversationType(MessageConstant.CONVERSATION_TYPE_GROUP);
        message.setSenderId(senderId);
        message.setReceiverId(groupId);
        message.setContentType(contentType);
        message.setContent(content);
        message.setExtra(extra);
        message.setIsRecalled(MessageConstant.IS_RECALLED_NO);
        message.setStatus(MessageConstant.STATUS_NORMAL);
        message.setSendTime(LocalDateTime.now());
        
        if (messageMapper.insert(message) > 0) {
            return message;
        }
        
        return null;
    }
    
    @Override
    @Transactional
    public boolean recallMessage(Long messageId, Long operatorId) {
        if (messageId == null || operatorId == null) {
            return false;
        }
        
        Message message = getMessage(messageId);
        if (message == null) {
            return false;
        }
        
        // 私聊消息只能由发送者撤回
        if (message.getConversationType() == MessageConstant.CONVERSATION_TYPE_PRIVATE 
                && !message.getSenderId().equals(operatorId)) {
            return false;
        }
        
        // 群聊消息的撤回权限验证
        if (message.getConversationType() == MessageConstant.CONVERSATION_TYPE_GROUP) {
            Long groupId = message.getReceiverId(); // 群聊中，receiverId 存储的是群组 ID
            
            // 如果是消息发送者自己撤回
            if (message.getSenderId().equals(operatorId)) {
                // 检查用户是否还在群中
                if (!chatGroupService.isUserInGroup(groupId, operatorId)) {
                    return false;
                }
                // 消息发送者可以撤回自己的消息
                // 可以在这里添加时间限制，如2分钟内才能撤回
                LocalDateTime now = LocalDateTime.now();
                if (now.minusMinutes(2).isAfter(message.getSendTime())) {
                    return false; // 超过2分钟不允许撤回
                }
            } else {
                // 非消息发送者（管理员或群主）撤回他人消息
                GroupMember operator = groupMemberMapper.selectByGroupIdAndUserId(groupId, operatorId);
                if (operator == null) {
                    return false; // 操作者不在群中
                }
                
                // 只有群主或管理员可以撤回他人消息
                if (operator.getRole() != GroupConstant.ROLE_OWNER && 
                    operator.getRole() != GroupConstant.ROLE_ADMIN) {
                    return false;
                }
            }
        }
        
        return messageMapper.updateRecallStatus(messageId, MessageConstant.IS_RECALLED_YES) > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteMessage(Long messageId) {
        if (messageId == null) {
            return false;
        }
        
        return messageMapper.updateStatus(messageId, MessageConstant.STATUS_DELETED) > 0;
    }
    
    @Override
    public Message getMessage(Long messageId) {
        if (messageId == null) {
            return null;
        }
        
        return messageMapper.selectById(messageId);
    }
    
    @Override
    public List<Message> getPrivateMessageHistory(Long userId, Long friendId, Integer limit) {
        if (userId == null || friendId == null) {
            return Collections.emptyList();
        }
        
        if (limit == null || limit <= 0) {
            limit = 20; // 默认获取20条
        }
        
        return messageMapper.selectPrivateMessages(userId, friendId, limit);
    }
    
    @Override
    public List<Message> getPrivateMessageHistoryBeforeId(Long userId, Long friendId, Long beforeMessageId, Integer limit) {
        if (userId == null || friendId == null || beforeMessageId == null) {
            return Collections.emptyList();
        }
        
        if (limit == null || limit <= 0) {
            limit = 20; // 默认获取20条
        }
        
        return messageMapper.selectPrivateMessagesBeforeId(userId, friendId, beforeMessageId, limit);
    }
    
    @Override
    public List<Message> getGroupMessageHistory(Long groupId, Integer limit) {
        if (groupId == null) {
            return Collections.emptyList();
        }
        
        if (limit == null || limit <= 0) {
            limit = 20; // 默认获取20条
        }
        
        return messageMapper.selectGroupMessages(groupId, limit);
    }
    
    @Override
    public List<Message> getGroupMessageHistoryBeforeId(Long groupId, Long beforeMessageId, Integer limit) {
        if (groupId == null || beforeMessageId == null) {
            return Collections.emptyList();
        }
        
        if (limit == null || limit <= 0) {
            limit = 20; // 默认获取20条
        }
        
        return messageMapper.selectGroupMessagesBeforeId(groupId, beforeMessageId, limit);
    }
    
    @Override
    public boolean saveMessage(Message message) {
        if (message == null) {
            return false;
        }
        
        return messageMapper.insert(message) > 0;
    }
    
    @Override
    public List<Message> getPrivateMessages(String userPhone, String otherPhone, int limit, int offset) {
        if (userPhone == null || otherPhone == null) {
            return Collections.emptyList();
        }
        
        if (limit <= 0) {
            limit = 20; // 默认获取20条
        }
        
        return messageMapper.selectPrivateMessagesByPhone(userPhone, otherPhone, limit, offset);
    }
    
    @Override
    public List<Message> getGroupMessages(Long groupId, int limit, int offset) {
        if (groupId == null) {
            return Collections.emptyList();
        }
        
        if (limit <= 0) {
            limit = 20; // 默认获取20条
        }
        
        return messageMapper.selectGroupMessagesByOffset(groupId, limit, offset);
    }
    
    @Override
    public boolean markMessageAsRead(Long messageId, String readerPhone) {
        if (messageId == null || readerPhone == null) {
            return false;
        }
        
        Message message = getMessage(messageId);
        if (message == null) {
            return false;
        }
        
        // 检查是否是接收者
        // 这里需要根据实际业务逻辑调整，可能需要通过手机号查找用户ID进行比较
        // 假设已有方法将手机号转换为用户ID
        // Long readerId = userService.getUserIdByPhone(readerPhone);
        
        // 标记为已读
        return messageMapper.updateReadStatus(messageId, MessageConstant.STATUS_NORMAL) > 0;
    }
    
    @Override
    public boolean recallMessage(Long messageId, String operatorPhone) {
        if (messageId == null || operatorPhone == null) {
            return false;
        }
        
        Message message = getMessage(messageId);
        if (message == null) {
            return false;
        }
        
        // 假设已有方法将手机号转换为用户ID
        // Long operatorId = userService.getUserIdByPhone(operatorPhone);
        
        // 这里简化处理，实际应该通过手机号获取用户ID后再调用已有的recallMessage方法
        // return recallMessage(messageId, operatorId);
        
        // 临时实现
        // 私聊消息只能由发送者撤回
        if (message.getConversationType() == MessageConstant.CONVERSATION_TYPE_PRIVATE) {
            // 检查是否是发送者
            // 假设已有方法将手机号转换为用户ID进行比较
            // if (!message.getSenderId().equals(operatorId)) {
            //    return false;
            // }
        }
        
        // 群聊消息撤回权限验证
        if (message.getConversationType() == MessageConstant.CONVERSATION_TYPE_GROUP) {
            // 检查用户是否在群中，是否有权限等
            // 与上面的recallMessage(Long messageId, Long operatorId)方法类似
        }
        
        return messageMapper.updateRecallStatus(messageId, MessageConstant.IS_RECALLED_YES) > 0;
    }
    
    @Override
    public int getUnreadMessageCount(String userPhone) {
        if (userPhone == null) {
            return 0;
        }
        
        // 假设已有方法将手机号转换为用户ID
        // Long userId = userService.getUserIdByPhone(userPhone);
        
        // 返回未读消息数量
        return messageMapper.countUnreadMessages(userPhone);
    }
    
    @Override
    public boolean deleteMessage(Long messageId, String operatorPhone) {
        if (messageId == null || operatorPhone == null) {
            return false;
        }
        
        Message message = getMessage(messageId);
        if (message == null) {
            return false;
        }
        
        // 假设已有方法将手机号转换为用户ID
        // Long operatorId = userService.getUserIdByPhone(operatorPhone);
        
        // 检查权限（只有消息发送者或接收者可以删除）
        // 这里简化处理，实际应该通过手机号获取用户ID后再进行比较
        
        return messageMapper.updateStatus(messageId, MessageConstant.STATUS_DELETED) > 0;
    }
    
    @Override
    public List<Message> getOfflineMessages(String userPhone) {
        if (userPhone == null) {
            return Collections.emptyList();
        }
        
        // 获取用户的离线消息
        // 这里假设messageMapper中有相应的方法
        return messageMapper.selectOfflineMessages(userPhone);
    }
    
    @Override
    public boolean confirmOfflineMessages(String userPhone, List<Long> messageIds) {
        if (userPhone == null || messageIds == null || messageIds.isEmpty()) {
            return false;
        }
        
        // 确认接收离线消息
        // 这里假设messageMapper中有相应的方法
        return messageMapper.confirmOfflineMessages(userPhone, messageIds) > 0;
    }
} 