package com.campus.im.service;

import com.campus.im.entity.Message;

import java.util.List;
import java.util.Map;

/**
 * 消息同步服务接口
 * 处理多设备消息同步
 */
public interface MessageSyncService {
    
    /**
     * 获取用户所有设备的同步状态
     *
     * @param userPhone 用户手机号
     * @return 设备同步状态，key为设备ID，value为最后同步的消息ID
     */
    Map<String, Long> getDeviceSyncStatus(String userPhone);
    
    /**
     * 更新设备同步状态
     *
     * @param userPhone 用户手机号
     * @param deviceId 设备ID
     * @param lastMessageId 最后同步的消息ID
     * @return 是否成功
     */
    boolean updateDeviceSyncStatus(String userPhone, String deviceId, Long lastMessageId);
    
    /**
     * 获取设备需要同步的消息
     *
     * @param userPhone 用户手机号
     * @param deviceId 设备ID
     * @param lastSyncMessageId 上次同步的消息ID
     * @param limit 消息数量限制
     * @return 需要同步的消息列表
     */
    List<Message> getSyncMessages(String userPhone, String deviceId, Long lastSyncMessageId, Integer limit);
    
    /**
     * 注册设备
     *
     * @param userPhone 用户手机号
     * @param deviceId 设备ID
     * @return 是否成功
     */
    boolean registerDevice(String userPhone, String deviceId);
    
    /**
     * 注销设备
     *
     * @param userPhone 用户手机号
     * @param deviceId 设备ID
     * @return 是否成功
     */
    boolean unregisterDevice(String userPhone, String deviceId);
} 