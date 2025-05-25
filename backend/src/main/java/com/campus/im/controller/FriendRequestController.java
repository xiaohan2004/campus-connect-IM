package com.campus.im.controller;

import com.campus.im.entity.FriendRequest;
import com.campus.im.entity.Result;
import com.campus.im.entity.ResultCode;
import com.campus.im.entity.User;
import com.campus.im.service.FriendRequestService;
import com.campus.im.service.UserService;
import com.campus.im.util.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 好友请求控制器
 */
@RestController
@RequestMapping("/api/friend/request")
public class FriendRequestController {

    @Autowired
    private FriendRequestService friendRequestService;

    @Autowired
    private UserService userService;

    /**
     * 发送好友请求
     *
     * @param params 参数，包含toUserId、message
     * @param request HTTP请求
     * @return 发送结果
     */
    @PostMapping("/send")
    public Result sendFriendRequest(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long fromUserId = AuthUtil.getCurrentUserId(request);
        if (fromUserId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "发送者ID不能为空");
        }

        Long toUserId = Long.valueOf(params.get("toUserId").toString());
        String message = params.get("message") != null ? params.get("message").toString() : null;

        // 检查是否已经发送过请求
        boolean hasPendingRequest = friendRequestService.hasPendingFriendRequest(fromUserId, toUserId);
        if (hasPendingRequest) {
            return Result.error(ResultCode.PARAM_ERROR, "已经发送过好友请求");
        }

        // 发送好友请求
        boolean success = friendRequestService.sendFriendRequest(fromUserId, toUserId, message);
        if (success) {
            return Result.success();
        } else {
            return Result.error("发送好友请求失败");
        }
    }

    /**
     * 接受好友请求
     *
     * @param requestId 请求ID
     * @param params 参数，包含remark、groupId
     * @param request HTTP请求
     * @return 接受结果
     */
    @PostMapping("/{requestId}/accept")
    public Result acceptFriendRequest(
            @PathVariable Long requestId,
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        String remark = params.get("remark") != null ? params.get("remark").toString() : null;
        Long groupId = params.get("groupId") != null ? Long.valueOf(params.get("groupId").toString()) : null;

        // 接受好友请求
        boolean success = friendRequestService.acceptFriendRequest(requestId, remark, groupId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("接受好友请求失败");
        }
    }

    /**
     * 拒绝好友请求
     *
     * @param requestId 请求ID
     * @param request HTTP请求
     * @return 拒绝结果
     */
    @PostMapping("/{requestId}/reject")
    public Result rejectFriendRequest(@PathVariable Long requestId, HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        // 拒绝好友请求
        boolean success = friendRequestService.rejectFriendRequest(requestId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("拒绝好友请求失败");
        }
    }

    /**
     * 获取收到的好友请求列表
     *
     * @param request HTTP请求
     * @return 好友请求列表
     */
    @GetMapping("/received")
    public Result getReceivedFriendRequests(HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        List<FriendRequest> requests = friendRequestService.getReceivedFriendRequests(userId);
        return Result.success(requests);
    }

    /**
     * 获取发送的好友请求列表
     *
     * @param request HTTP请求
     * @return 好友请求列表
     */
    @GetMapping("/sent")
    public Result getSentFriendRequests(HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        List<FriendRequest> requests = friendRequestService.getSentFriendRequests(userId);
        return Result.success(requests);
    }

    /**
     * 获取好友请求详情
     *
     * @param requestId 请求ID
     * @return 好友请求详情
     */
    @GetMapping("/{requestId}")
    public Result getFriendRequest(@PathVariable Long requestId) {
        FriendRequest friendRequest = friendRequestService.getFriendRequest(requestId);
        if (friendRequest != null) {
            return Result.success(friendRequest);
        } else {
            return Result.error(ResultCode.NOT_FOUND, "好友请求不存在");
        }
    }

    /**
     * 取消发送的好友请求
     *
     * @param requestId 请求ID
     * @param request HTTP请求
     * @return 取消结果
     */
    @DeleteMapping("/{requestId}")
    public Result cancelFriendRequest(@PathVariable Long requestId, HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        // 获取好友请求
        FriendRequest friendRequest = friendRequestService.getFriendRequest(requestId);
        if (friendRequest == null) {
            return Result.error(ResultCode.NOT_FOUND, "好友请求不存在");
        }
        
        // 检查是否是发送者
        if (!friendRequest.getFromUserId().equals(userId)) {
            return Result.error(ResultCode.FORBIDDEN, "没有权限取消此好友请求");
        }
        
        // 拒绝好友请求（取消）
        boolean success = friendRequestService.rejectFriendRequest(requestId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("取消好友请求失败");
        }
    }

    /**
     * 通过手机号查找用户
     *
     * @param phone 手机号
     * @return 用户信息
     */
    @GetMapping("/search/{phone}")
    public Result searchUserByPhone(@PathVariable String phone) {
        User user = userService.getUserByPhone(phone);
        if (user == null) {
            return Result.error(ResultCode.NOT_FOUND, "用户不存在");
        }

        // 只返回基本信息
        User basicUser = new User();
        basicUser.setId(user.getId());
        basicUser.setNickname(user.getNickname());
        basicUser.setAvatar(user.getAvatar());
        basicUser.setStatus(user.getStatus());
        
        return Result.success(basicUser);
    }
}