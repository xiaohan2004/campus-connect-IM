package com.campus.im.controller;

import com.campus.im.entity.Message;
import com.campus.im.entity.Result;
import com.campus.im.entity.ResultCode;
import com.campus.im.service.MessageService;
import com.campus.im.service.UserService;
import com.campus.im.util.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 消息控制器
 */
@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    /**
     * 发送私聊消息
     *
     * @param params 参数，包含receiverPhone、contentType、content、extra
     * @param request HTTP请求
     * @return 发送结果
     */
    @PostMapping("/private/send")
    public Result sendPrivateMessage(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        String senderPhone = AuthUtil.getCurrentUserPhone(request);
        String receiverPhone = params.get("receiverPhone").toString();
        Integer contentType = Integer.valueOf(params.get("contentType").toString());
        String content = params.get("content").toString();
        String extra = params.get("extra") != null ? params.get("extra").toString() : null;

        // 获取发送者ID
        Long senderId = AuthUtil.getCurrentUserId(request);
        if (senderId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "发送者ID不能为空");
        }

        // 获取接收者ID
        Long receiverId = userService.getUserByPhone(receiverPhone).getId();
        if (receiverId == null) {
            return Result.error(ResultCode.NOT_FOUND, "接收者不存在");
        }

        // 发送消息
        Message message = messageService.sendPrivateMessage(senderId, receiverId, contentType, content, extra);
        if (message != null) {
            return Result.success(message);
        } else {
            return Result.error("发送失败");
        }
    }

    /**
     * 发送群聊消息
     *
     * @param params 参数，包含groupId、contentType、content、extra
     * @param request HTTP请求
     * @return 发送结果
     */
    @PostMapping("/group/send")
    public Result sendGroupMessage(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long groupId = Long.valueOf(params.get("groupId").toString());
        Integer contentType = Integer.valueOf(params.get("contentType").toString());
        String content = params.get("content").toString();
        String extra = params.get("extra") != null ? params.get("extra").toString() : null;

        // 获取发送者ID
        Long senderId = AuthUtil.getCurrentUserId(request);
        if (senderId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "发送者ID不能为空");
        }

        // 发送消息
        Message message = messageService.sendGroupMessage(senderId, groupId, contentType, content, extra);
        if (message != null) {
            return Result.success(message);
        } else {
            return Result.error("发送失败");
        }
    }

    /**
     * 获取私聊消息列表
     *
     * @param otherPhone 对方手机号
     * @param limit 消息数量限制
     * @param offset 偏移量
     * @param request HTTP请求
     * @return 消息列表
     */
    @GetMapping("/private/{otherPhone}")
    public Result getPrivateMessages(
            @PathVariable String otherPhone,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset,
            HttpServletRequest request) {
        String userPhone = AuthUtil.getCurrentUserPhone(request);
        List<Message> messages = messageService.getPrivateMessages(userPhone, otherPhone, limit, offset);
        return Result.success(messages);
    }

    /**
     * 获取群聊消息列表
     *
     * @param groupId 群组ID
     * @param limit 消息数量限制
     * @param offset 偏移量
     * @param request HTTP请求
     * @return 消息列表
     */
    @GetMapping("/group/{groupId}")
    public Result getGroupMessages(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset,
            HttpServletRequest request) {
        List<Message> messages = messageService.getGroupMessages(groupId, limit, offset);
        return Result.success(messages);
    }

    /**
     * 标记消息为已读
     *
     * @param messageId 消息ID
     * @param request HTTP请求
     * @return 标记结果
     */
    @PutMapping("/read/{messageId}")
    public Result markMessageAsRead(@PathVariable Long messageId, HttpServletRequest request) {
        String readerPhone = AuthUtil.getCurrentUserPhone(request);
        boolean success = messageService.markMessageAsRead(messageId, readerPhone);
        if (success) {
            return Result.success();
        } else {
            return Result.error("标记已读失败");
        }
    }

    /**
     * 撤回消息
     *
     * @param messageId 消息ID
     * @param request HTTP请求
     * @return 撤回结果
     */
    @PutMapping("/recall/{messageId}")
    public Result recallMessage(@PathVariable Long messageId, HttpServletRequest request) {
        String operatorPhone = AuthUtil.getCurrentUserPhone(request);
        boolean success = messageService.recallMessage(messageId, operatorPhone);
        if (success) {
            return Result.success();
        } else {
            return Result.error("撤回失败");
        }
    }

    /**
     * 删除消息
     *
     * @param messageId 消息ID
     * @param request HTTP请求
     * @return 删除结果
     */
    @DeleteMapping("/{messageId}")
    public Result deleteMessage(@PathVariable Long messageId, HttpServletRequest request) {
        String operatorPhone = AuthUtil.getCurrentUserPhone(request);
        boolean success = messageService.deleteMessage(messageId, operatorPhone);
        if (success) {
            return Result.success();
        } else {
            return Result.error("删除失败");
        }
    }

    /**
     * 获取未读消息数
     *
     * @param request HTTP请求
     * @return 未读消息数
     */
    @GetMapping("/unread/count")
    public Result getUnreadMessageCount(HttpServletRequest request) {
        String userPhone = AuthUtil.getCurrentUserPhone(request);
        int count = messageService.getUnreadMessageCount(userPhone);
        return Result.success(count);
    }

    /**
     * 获取离线消息
     *
     * @param request HTTP请求
     * @return 离线消息列表
     */
    @GetMapping("/offline")
    public Result getOfflineMessages(HttpServletRequest request) {
        String userPhone = AuthUtil.getCurrentUserPhone(request);
        List<Message> messages = messageService.getOfflineMessages(userPhone);
        return Result.success(messages);
    }

    /**
     * 确认接收离线消息
     *
     * @param params 参数，包含messageIds
     * @param request HTTP请求
     * @return 确认结果
     */
    @PostMapping("/offline/confirm")
    public Result confirmOfflineMessages(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        String userPhone = AuthUtil.getCurrentUserPhone(request);
        @SuppressWarnings("unchecked")
        List<Long> messageIds = (List<Long>) params.get("messageIds");
        
        boolean success = messageService.confirmOfflineMessages(userPhone, messageIds);
        if (success) {
            return Result.success();
        } else {
            return Result.error("确认离线消息失败");
        }
    }

    /**
     * 获取消息详情
     *
     * @param messageId 消息ID
     * @return 消息详情
     */
    @GetMapping("/{messageId}")
    public Result getMessage(@PathVariable Long messageId) {
        Message message = messageService.getMessage(messageId);
        if (message != null) {
            return Result.success(message);
        } else {
            return Result.error(ResultCode.NOT_FOUND, "消息不存在");
        }
    }
} 