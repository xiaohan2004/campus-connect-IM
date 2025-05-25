package com.campus.im.controller;

import com.campus.im.entity.Friendship;
import com.campus.im.entity.Result;
import com.campus.im.entity.ResultCode;
import com.campus.im.entity.User;
import com.campus.im.service.FriendshipService;
import com.campus.im.service.UserService;
import com.campus.im.util.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 好友关系控制器
 */
@RestController
@RequestMapping("/api/friendship")
public class FriendshipController {

    @Autowired
    private FriendshipService friendshipService;

    @Autowired
    private UserService userService;

    /**
     * 获取好友列表
     *
     * @param request HTTP请求
     * @return 好友列表
     */
    @GetMapping("/list")
    public Result getFriendList(HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        List<User> friendList = friendshipService.getFriendUserList(userId);
        return Result.success(friendList);
    }

    /**
     * 添加好友
     *
     * @param params 参数，包含friendId、remark、groupId
     * @param request HTTP请求
     * @return 添加结果
     */
    @PostMapping("/add")
    public Result addFriend(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        Long friendId = Long.valueOf(params.get("friendId").toString());
        String remark = params.get("remark") != null ? params.get("remark").toString() : null;
        Long groupId = params.get("groupId") != null ? Long.valueOf(params.get("groupId").toString()) : null;

        // 检查好友是否存在
        User friend = userService.getUserById(friendId);
        if (friend == null) {
            return Result.error(ResultCode.NOT_FOUND, "好友不存在");
        }

        // 检查是否已经是好友
        if (friendshipService.isFriend(userId, friendId)) {
            return Result.error(ResultCode.PARAM_ERROR, "已经是好友关系");
        }

        // 添加好友
        boolean success = friendshipService.addFriend(userId, friendId, remark, groupId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("添加好友失败");
        }
    }

    /**
     * 删除好友
     *
     * @param friendId 好友ID
     * @param request HTTP请求
     * @return 删除结果
     */
    @DeleteMapping("/{friendId}")
    public Result deleteFriend(@PathVariable Long friendId, HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        // 检查是否是好友
        if (!friendshipService.isFriend(userId, friendId)) {
            return Result.error(ResultCode.PARAM_ERROR, "不是好友关系");
        }

        // 删除好友
        boolean success = friendshipService.deleteFriend(userId, friendId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("删除好友失败");
        }
    }

    /**
     * 更新好友备注
     *
     * @param params 参数，包含friendId、remark
     * @param request HTTP请求
     * @return 更新结果
     */
    @PutMapping("/remark")
    public Result updateFriendRemark(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        Long friendId = Long.valueOf(params.get("friendId").toString());
        String remark = params.get("remark").toString();

        // 检查是否是好友
        if (!friendshipService.isFriend(userId, friendId)) {
            return Result.error(ResultCode.PARAM_ERROR, "不是好友关系");
        }

        // 更新好友备注
        boolean success = friendshipService.updateFriendRemark(userId, friendId, remark);
        if (success) {
            return Result.success();
        } else {
            return Result.error("更新备注失败");
        }
    }

    /**
     * 更新好友分组
     *
     * @param params 参数，包含friendId、groupId
     * @param request HTTP请求
     * @return 更新结果
     */
    @PutMapping("/group")
    public Result updateFriendGroup(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        Long friendId = Long.valueOf(params.get("friendId").toString());
        Long groupId = Long.valueOf(params.get("groupId").toString());

        // 检查是否是好友
        if (!friendshipService.isFriend(userId, friendId)) {
            return Result.error(ResultCode.PARAM_ERROR, "不是好友关系");
        }

        // 更新好友分组
        boolean success = friendshipService.updateFriendGroup(userId, friendId, groupId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("更新分组失败");
        }
    }

    /**
     * 更新好友状态（拉黑/取消拉黑）
     *
     * @param params 参数，包含friendId、status
     * @param request HTTP请求
     * @return 更新结果
     */
    @PutMapping("/status")
    public Result updateFriendStatus(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        Long friendId = Long.valueOf(params.get("friendId").toString());
        Integer status = Integer.valueOf(params.get("status").toString());

        // 检查是否是好友
        if (!friendshipService.isFriend(userId, friendId)) {
            return Result.error(ResultCode.PARAM_ERROR, "不是好友关系");
        }

        // 更新好友状态
        boolean success = friendshipService.updateFriendStatus(userId, friendId, status);
        if (success) {
            return Result.success();
        } else {
            return Result.error("更新状态失败");
        }
    }

    /**
     * 获取好友关系
     *
     * @param friendId 好友ID
     * @param request HTTP请求
     * @return 好友关系
     */
    @GetMapping("/{friendId}")
    public Result getFriendship(@PathVariable Long friendId, HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        Friendship friendship = friendshipService.getFriendship(userId, friendId);
        if (friendship == null) {
            return Result.error(ResultCode.NOT_FOUND, "好友关系不存在");
        }

        return Result.success(friendship);
    }

    /**
     * 检查是否为好友
     *
     * @param friendId 好友ID
     * @param request HTTP请求
     * @return 是否为好友
     */
    @GetMapping("/check/{friendId}")
    public Result checkFriendship(@PathVariable Long friendId, HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        boolean isFriend = friendshipService.isFriend(userId, friendId);
        return Result.success(isFriend);
    }
} 