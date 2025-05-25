package com.campus.im.service.impl;

import com.campus.im.entity.Message;
import com.campus.im.mapper.MessageMapper;
import com.campus.im.service.MessageSyncService;
import com.campus.im.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 消息同步服务实现类
 */
@Service
public class MessageSyncServiceImpl implements MessageSyncService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private MessageMapper messageMapper;
    
    @Autowired
    private UserService userService;
    
    // Redis key前缀
    private static final String DEVICE_SYNC_KEY_PREFIX = "user:devices:sync:";
    private static final String USER_DEVICES_KEY_PREFIX = "user:devices:";
    
    @Override
    public Map<String, Long> getDeviceSyncStatus(String userPhone) {
        if (userPhone == null) {
            return Collections.emptyMap();
        }
        
        // 从Redis获取用户所有设备的同步状态
        String key = DEVICE_SYNC_KEY_PREFIX + userPhone;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        
        Map<String, Long> result = new HashMap<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            String deviceId = entry.getKey().toString();
            Long lastMessageId = Long.valueOf(entry.getValue().toString());
            result.put(deviceId, lastMessageId);
        }
        
        return result;
    }
    
    @Override
    public boolean updateDeviceSyncStatus(String userPhone, String deviceId, Long lastMessageId) {
        if (userPhone == null || deviceId == null || lastMessageId == null) {
            return false;
        }
        
        // 更新Redis中的设备同步状态
        String key = DEVICE_SYNC_KEY_PREFIX + userPhone;
        redisTemplate.opsForHash().put(key, deviceId, lastMessageId);
        
        return true;
    }
    
    @Override
    public List<Message> getSyncMessages(String userPhone, String deviceId, Long lastSyncMessageId, Integer limit) {
        if (userPhone == null || deviceId == null) {
            return Collections.emptyList();
        }
        
        if (limit == null || limit <= 0) {
            limit = 50; // 默认获取50条
        }
        
        // 获取用户ID
        Long userId = userService.getUserByPhone(userPhone).getId();
        if (userId == null) {
            return Collections.emptyList();
        }
        
        // 获取需要同步的消息
        // 这里需要根据实际业务逻辑调整
        // 假设messageMapper中有相应的方法
        // return messageMapper.selectMessagesAfterIdForUser(userId, lastSyncMessageId, limit);
        
        // 临时返回空列表，实际实现需要查询数据库
        return Collections.emptyList();
    }
    
    @Override
    public boolean registerDevice(String userPhone, String deviceId) {
        if (userPhone == null || deviceId == null) {
            return false;
        }
        
        // 将设备ID添加到用户设备列表
        String key = USER_DEVICES_KEY_PREFIX + userPhone;
        redisTemplate.opsForSet().add(key, deviceId);
        
        // 设置初始同步状态
        String syncKey = DEVICE_SYNC_KEY_PREFIX + userPhone;
        redisTemplate.opsForHash().putIfAbsent(syncKey, deviceId, 0L);
        
        return true;
    }
    
    @Override
    public boolean unregisterDevice(String userPhone, String deviceId) {
        if (userPhone == null || deviceId == null) {
            return false;
        }
        
        // 从用户设备列表中移除设备ID
        String key = USER_DEVICES_KEY_PREFIX + userPhone;
        redisTemplate.opsForSet().remove(key, deviceId);
        
        // 移除设备同步状态
        String syncKey = DEVICE_SYNC_KEY_PREFIX + userPhone;
        redisTemplate.opsForHash().delete(syncKey, deviceId);
        
        return true;
    }
} 