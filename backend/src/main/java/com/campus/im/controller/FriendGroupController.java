package com.campus.im.controller;

import com.campus.im.entity.FriendGroup;
import com.campus.im.common.Result;
import com.campus.im.common.enumeration.ResultCode;
import com.campus.im.service.FriendGroupService;
import com.campus.im.util.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 好友分组控制器
 */
@RestController
@RequestMapping("/api/friend/group")
public class FriendGroupController {

    @Autowired
    private FriendGroupService friendGroupService;

    /**
     * 创建好友分组
     *
     * @param params 参数，包含name
     * @param request HTTP请求
     * @return 创建结果
     */
    @PostMapping("/create")
    public Result createFriendGroup(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        String name = params.get("name").toString();

        FriendGroup friendGroup = friendGroupService.createFriendGroup(userId, name);
        if (friendGroup != null) {
            return Result.success(friendGroup);
        } else {
            return Result.error("创建好友分组失败");
        }
    }

    /**
     * 更新好友分组
     *
     * @param groupId 分组ID
     * @param params 参数，包含name
     * @param request HTTP请求
     * @return 更新结果
     */
    @PutMapping("/{groupId}")
    public Result updateFriendGroup(
            @PathVariable Long groupId,
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        String name = params.get("name").toString();

        boolean success = friendGroupService.updateFriendGroup(groupId, userId, name);
        if (success) {
            return Result.success();
        } else {
            return Result.error("更新好友分组失败");
        }
    }

    /**
     * 删除好友分组
     *
     * @param groupId 分组ID
     * @param request HTTP请求
     * @return 删除结果
     */
    @DeleteMapping("/{groupId}")
    public Result deleteFriendGroup(@PathVariable Long groupId, HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        boolean success = friendGroupService.deleteFriendGroup(groupId, userId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("删除好友分组失败");
        }
    }

    /**
     * 获取用户的好友分组列表
     *
     * @param request HTTP请求
     * @return 好友分组列表
     */
    @GetMapping("/list")
    public Result getFriendGroups(HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        List<FriendGroup> groups = friendGroupService.getFriendGroups(userId);
        return Result.success(groups);
    }

    /**
     * 获取好友分组详情
     *
     * @param groupId 分组ID
     * @param request HTTP请求
     * @return 好友分组详情
     */
    @GetMapping("/{groupId}")
    public Result getFriendGroup(@PathVariable Long groupId, HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        FriendGroup group = friendGroupService.getFriendGroup(groupId, userId);
        if (group == null) {
            return Result.error(ResultCode.NOT_FOUND, "好友分组不存在");
        }
        return Result.success(group);
    }

    /**
     * 移动好友到指定分组
     *
     * @param params 参数，包含friendId、groupId
     * @param request HTTP请求
     * @return 移动结果
     */
    @PutMapping("/move")
    public Result moveFriendToGroup(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        Long friendId = Long.valueOf(params.get("friendId").toString());
        Long groupId = Long.valueOf(params.get("groupId").toString());

        boolean success = friendGroupService.moveFriendToGroup(userId, friendId, groupId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("移动好友到指定分组失败");
        }
    }
}