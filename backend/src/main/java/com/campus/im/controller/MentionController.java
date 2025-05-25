package com.campus.im.controller;

import com.campus.im.common.Result;
import com.campus.im.common.enumeration.ResultCode;
import com.campus.im.service.MentionService;
import com.campus.im.util.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @功能控制器
 */
@RestController
@RequestMapping("/api/mention")
public class MentionController {

    @Autowired
    private MentionService mentionService;

    /**
     * 获取用户在群组中被@的消息
     *
     * @param groupId 群组ID
     * @param limit 消息数量限制
     * @param offset 偏移量
     * @param request HTTP请求
     * @return 消息ID列表
     */
    @GetMapping("/group/{groupId}")
    public Result getUserMentionedMessages(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset,
            HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        List<Long> messageIds = mentionService.getUserMentionedMessages(userId, groupId, limit, offset);
        return Result.success(messageIds);
    }

    /**
     * 获取用户在所有群组中被@的消息
     *
     * @param limit 消息数量限制
     * @param offset 偏移量
     * @param request HTTP请求
     * @return 消息ID和群组ID的映射
     */
    @GetMapping("/all")
    public Result getAllUserMentionedMessages(
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset,
            HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        Map<Long, Long> messages = mentionService.getAllUserMentionedMessages(userId, limit, offset);
        return Result.success(messages);
    }

    /**
     * 标记@消息为已读
     *
     * @param messageId 消息ID
     * @param request HTTP请求
     * @return 标记结果
     */
    @PutMapping("/read/{messageId}")
    public Result markMentionAsRead(@PathVariable Long messageId, HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        boolean success = mentionService.markMentionAsRead(userId, messageId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("标记已读失败");
        }
    }

    /**
     * 获取用户未读的@消息数量
     *
     * @param request HTTP请求
     * @return 未读@消息数量
     */
    @GetMapping("/unread/count")
    public Result getUnreadMentionCount(HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        int count = mentionService.getUnreadMentionCount(userId);
        return Result.success(count);
    }
} 