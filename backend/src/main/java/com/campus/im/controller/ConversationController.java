package com.campus.im.controller;

import com.campus.im.entity.Conversation;
import com.campus.im.entity.Result;
import com.campus.im.entity.ResultCode;
import com.campus.im.service.ConversationService;
import com.campus.im.util.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 会话控制器
 */
@RestController
@RequestMapping("/api/conversation")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    /**
     * 获取用户的会话列表
     *
     * @param request HTTP请求
     * @return 会话列表
     */
    @GetMapping("/list")
    public Result getConversationList(HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        List<Conversation> conversations = conversationService.getUserConversations(userId);
        return Result.success(conversations);
    }

    /**
     * 获取会话详情
     *
     * @param conversationId 会话ID
     * @param request HTTP请求
     * @return 会话详情
     */
    @GetMapping("/{conversationId}")
    public Result getConversation(@PathVariable Long conversationId, HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        Conversation conversation = conversationService.getConversation(conversationId, userId);
        if (conversation == null) {
            return Result.error(ResultCode.NOT_FOUND, "会话不存在");
        }
        return Result.success(conversation);
    }

    /**
     * 创建或获取私聊会话
     *
     * @param params 参数，包含targetUserId
     * @param request HTTP请求
     * @return 会话信息
     */
    @PostMapping("/private")
    public Result createOrGetPrivateConversation(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        Long targetUserId = Long.valueOf(params.get("targetUserId").toString());

        Conversation conversation = conversationService.createOrGetPrivateConversation(userId, targetUserId);
        if (conversation != null) {
            return Result.success(conversation);
        } else {
            return Result.error("创建会话失败");
        }
    }

    /**
     * 创建或获取群聊会话
     *
     * @param params 参数，包含groupId
     * @param request HTTP请求
     * @return 会话信息
     */
    @PostMapping("/group")
    public Result createOrGetGroupConversation(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        Long groupId = Long.valueOf(params.get("groupId").toString());

        Conversation conversation = conversationService.createOrGetGroupConversation(userId, groupId);
        if (conversation != null) {
            return Result.success(conversation);
        } else {
            return Result.error("创建会话失败");
        }
    }

    /**
     * 删除会话
     *
     * @param conversationId 会话ID
     * @param request HTTP请求
     * @return 删除结果
     */
    @DeleteMapping("/{conversationId}")
    public Result deleteConversation(@PathVariable Long conversationId, HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        boolean success = conversationService.deleteConversation(conversationId, userId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("删除会话失败");
        }
    }

    /**
     * 置顶会话
     *
     * @param conversationId 会话ID
     * @param params 参数，包含isTop
     * @param request HTTP请求
     * @return 置顶结果
     */
    @PutMapping("/{conversationId}/top")
    public Result topConversation(
            @PathVariable Long conversationId,
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        Boolean isTop = Boolean.valueOf(params.get("isTop").toString());

        boolean success = conversationService.topConversation(conversationId, userId, isTop);
        if (success) {
            return Result.success();
        } else {
            return Result.error("设置置顶失败");
        }
    }

    /**
     * 设置会话免打扰
     *
     * @param conversationId 会话ID
     * @param params 参数，包含isMuted
     * @param request HTTP请求
     * @return 设置结果
     */
    @PutMapping("/{conversationId}/mute")
    public Result muteConversation(
            @PathVariable Long conversationId,
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        Boolean isMuted = Boolean.valueOf(params.get("isMuted").toString());

        boolean success = conversationService.muteConversation(conversationId, userId, isMuted);
        if (success) {
            return Result.success();
        } else {
            return Result.error("设置免打扰失败");
        }
    }

    /**
     * 清空会话消息
     *
     * @param conversationId 会话ID
     * @param request HTTP请求
     * @return 清空结果
     */
    @PutMapping("/{conversationId}/clear")
    public Result clearConversationMessages(@PathVariable Long conversationId, HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        boolean success = conversationService.clearConversationMessages(conversationId, userId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("清空会话消息失败");
        }
    }

    /**
     * 标记会话已读
     *
     * @param conversationId 会话ID
     * @param request HTTP请求
     * @return 标记结果
     */
    @PutMapping("/{conversationId}/read")
    public Result markConversationAsRead(@PathVariable Long conversationId, HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        boolean success = conversationService.markConversationAsRead(conversationId, userId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("标记已读失败");
        }
    }
}