package com.campus.im.controller;

import com.campus.im.entity.Message;
import com.campus.im.entity.Result;
import com.campus.im.service.MessageSyncService;
import com.campus.im.util.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 消息同步控制器
 */
@RestController
@RequestMapping("/api/message/sync")
public class MessageSyncController {

    @Autowired
    private MessageSyncService messageSyncService;

    /**
     * 注册设备
     *
     * @param params 参数，包含deviceId
     * @param request HTTP请求
     * @return 注册结果
     */
    @PostMapping("/device/register")
    public Result registerDevice(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        String userPhone = AuthUtil.getCurrentUserPhone(request);
        String deviceId = params.get("deviceId").toString();
        
        boolean success = messageSyncService.registerDevice(userPhone, deviceId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("设备注册失败");
        }
    }

    /**
     * 注销设备
     *
     * @param params 参数，包含deviceId
     * @param request HTTP请求
     * @return 注销结果
     */
    @PostMapping("/device/unregister")
    public Result unregisterDevice(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        String userPhone = AuthUtil.getCurrentUserPhone(request);
        String deviceId = params.get("deviceId").toString();
        
        boolean success = messageSyncService.unregisterDevice(userPhone, deviceId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("设备注销失败");
        }
    }

    /**
     * 获取设备同步状态
     *
     * @param request HTTP请求
     * @return 设备同步状态
     */
    @GetMapping("/status")
    public Result getDeviceSyncStatus(HttpServletRequest request) {
        String userPhone = AuthUtil.getCurrentUserPhone(request);
        Map<String, Long> status = messageSyncService.getDeviceSyncStatus(userPhone);
        return Result.success(status);
    }

    /**
     * 更新设备同步状态
     *
     * @param params 参数，包含deviceId, lastMessageId
     * @param request HTTP请求
     * @return 更新结果
     */
    @PostMapping("/status/update")
    public Result updateDeviceSyncStatus(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        String userPhone = AuthUtil.getCurrentUserPhone(request);
        String deviceId = params.get("deviceId").toString();
        Long lastMessageId = Long.valueOf(params.get("lastMessageId").toString());
        
        boolean success = messageSyncService.updateDeviceSyncStatus(userPhone, deviceId, lastMessageId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("更新同步状态失败");
        }
    }

    /**
     * 获取需要同步的消息
     *
     * @param deviceId 设备ID
     * @param lastSyncMessageId 上次同步的消息ID
     * @param limit 消息数量限制
     * @param request HTTP请求
     * @return 需要同步的消息列表
     */
    @GetMapping("/messages")
    public Result getSyncMessages(
            @RequestParam String deviceId,
            @RequestParam(required = false) Long lastSyncMessageId,
            @RequestParam(defaultValue = "50") Integer limit,
            HttpServletRequest request) {
        String userPhone = AuthUtil.getCurrentUserPhone(request);
        List<Message> messages = messageSyncService.getSyncMessages(userPhone, deviceId, lastSyncMessageId, limit);
        return Result.success(messages);
    }
} 