package com.campus.im.service.impl;

import com.campus.im.common.constant.MessageReceiptConstant;
import com.campus.im.entity.MessageReceipt;
import com.campus.im.mapper.MessageReceiptMapper;
import com.campus.im.service.MessageReceiptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 消息接收状态服务实现类
 */
@Service
public class MessageReceiptServiceImpl implements MessageReceiptService {
    
    @Autowired
    private MessageReceiptMapper messageReceiptMapper;
    
    @Override
    @Transactional
    public boolean addMessageReceipt(Long messageId, Long userId) {
        if (messageId == null || userId == null) {
            return false;
        }
        
        MessageReceipt receipt = new MessageReceipt();
        receipt.setMessageId(messageId);
        receipt.setUserId(userId);
        receipt.setIsRead(MessageReceiptConstant.IS_READ_NO);
        
        return messageReceiptMapper.insert(receipt) > 0;
    }
    
    @Override
    @Transactional
    public boolean addMessageReceipts(Long messageId, List<Long> userIds) {
        if (messageId == null || userIds == null || userIds.isEmpty()) {
            return false;
        }
        
        List<MessageReceipt> receipts = new ArrayList<>();
        for (Long userId : userIds) {
            MessageReceipt receipt = new MessageReceipt();
            receipt.setMessageId(messageId);
            receipt.setUserId(userId);
            receipt.setIsRead(MessageReceiptConstant.IS_READ_NO);
            receipts.add(receipt);
        }
        
        return messageReceiptMapper.batchInsert(receipts) > 0;
    }
    
    @Override
    @Transactional
    public boolean markAsRead(Long messageId, Long userId) {
        if (messageId == null || userId == null) {
            return false;
        }
        
        return messageReceiptMapper.updateReadStatus(messageId, userId, MessageReceiptConstant.IS_READ_YES) > 0;
    }
    
    @Override
    @Transactional
    public boolean markAsRead(List<Long> messageIds, Long userId) {
        if (messageIds == null || messageIds.isEmpty() || userId == null) {
            return false;
        }
        
        return messageReceiptMapper.batchUpdateReadStatus(messageIds, userId, MessageReceiptConstant.IS_READ_YES) > 0;
    }
    
    @Override
    public MessageReceipt getMessageReceipt(Long messageId, Long userId) {
        if (messageId == null || userId == null) {
            return null;
        }
        
        return messageReceiptMapper.selectByMessageIdAndUserId(messageId, userId);
    }
    
    @Override
    public List<MessageReceipt> getMessageReceipts(Long messageId) {
        if (messageId == null) {
            return Collections.emptyList();
        }
        
        return messageReceiptMapper.selectByMessageId(messageId);
    }
    
    @Override
    public List<Long> getReadUserIds(Long messageId) {
        if (messageId == null) {
            return Collections.emptyList();
        }
        
        return messageReceiptMapper.selectReadUserIdsByMessageId(messageId);
    }
    
    @Override
    public List<Long> getUnreadUserIds(Long messageId) {
        if (messageId == null) {
            return Collections.emptyList();
        }
        
        return messageReceiptMapper.selectUnreadUserIdsByMessageId(messageId);
    }
    
    @Override
    public int countRead(Long messageId) {
        if (messageId == null) {
            return 0;
        }
        
        return messageReceiptMapper.countReadByMessageId(messageId);
    }
    
    @Override
    public int countUnread(Long messageId) {
        if (messageId == null) {
            return 0;
        }
        
        return messageReceiptMapper.countUnreadByMessageId(messageId);
    }
} 