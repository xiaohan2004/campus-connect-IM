package com.campus.im.controller;

import com.campus.im.entity.ChatGroup;
import com.campus.im.entity.GroupMember;
import com.campus.im.entity.Result;
import com.campus.im.entity.ResultCode;
import com.campus.im.entity.User;
import com.campus.im.service.ChatGroupService;
import com.campus.im.util.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 群组控制器
 */
@RestController
@RequestMapping("/api/group")
public class ChatGroupController {

    @Autowired
    private ChatGroupService chatGroupService;

    /**
     * 创建群组
     *
     * @param params 参数，包含name、avatar、description、announcement、memberIds
     * @param request HTTP请求
     * @return 创建结果
     */
    @PostMapping("/create")
    public Result createGroup(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long creatorId = AuthUtil.getCurrentUserId(request);
        if (creatorId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "创建者ID不能为空");
        }

        String name = params.get("name").toString();
        String avatar = params.get("avatar") != null ? params.get("avatar").toString() : null;
        String description = params.get("description") != null ? params.get("description").toString() : null;
        String announcement = params.get("announcement") != null ? params.get("announcement").toString() : null;
        List<Long> memberIds = (List<Long>) params.get("memberIds");

        ChatGroup group = chatGroupService.createGroup(name, avatar, creatorId, description, announcement, memberIds);
        if (group != null) {
            return Result.success(group);
        } else {
            return Result.error("创建群组失败");
        }
    }

    /**
     * 解散群组
     *
     * @param groupId 群组ID
     * @param request HTTP请求
     * @return 解散结果
     */
    @DeleteMapping("/{groupId}")
    public Result disbandGroup(@PathVariable Long groupId, HttpServletRequest request) {
        Long operatorId = AuthUtil.getCurrentUserId(request);
        if (operatorId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "操作者ID不能为空");
        }

        boolean success = chatGroupService.disbandGroup(groupId, operatorId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("解散群组失败，可能没有权限");
        }
    }

    /**
     * 更新群组信息
     *
     * @param groupId 群组ID
     * @param params 参数，包含name、avatar、description、announcement
     * @param request HTTP请求
     * @return 更新结果
     */
    @PutMapping("/{groupId}")
    public Result updateGroupInfo(
            @PathVariable Long groupId,
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        Long operatorId = AuthUtil.getCurrentUserId(request);
        if (operatorId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "操作者ID不能为空");
        }

        String name = params.get("name") != null ? params.get("name").toString() : null;
        String avatar = params.get("avatar") != null ? params.get("avatar").toString() : null;
        String description = params.get("description") != null ? params.get("description").toString() : null;
        String announcement = params.get("announcement") != null ? params.get("announcement").toString() : null;

        boolean success = chatGroupService.updateGroupInfo(groupId, name, avatar, description, announcement, operatorId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("更新群组信息失败，可能没有权限");
        }
    }

    /**
     * 获取群组信息
     *
     * @param groupId 群组ID
     * @return 群组信息
     */
    @GetMapping("/{groupId}")
    public Result getGroupInfo(@PathVariable Long groupId) {
        ChatGroup group = chatGroupService.getGroupInfo(groupId);
        if (group != null) {
            return Result.success(group);
        } else {
            return Result.error(ResultCode.NOT_FOUND, "群组不存在");
        }
    }

    /**
     * 添加群成员
     *
     * @param groupId 群组ID
     * @param params 参数，包含userId
     * @param request HTTP请求
     * @return 添加结果
     */
    @PostMapping("/{groupId}/member")
    public Result addGroupMember(
            @PathVariable Long groupId,
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        Long operatorId = AuthUtil.getCurrentUserId(request);
        if (operatorId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "操作者ID不能为空");
        }

        Long userId = Long.valueOf(params.get("userId").toString());

        boolean success = chatGroupService.addGroupMember(groupId, userId, operatorId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("添加群成员失败，可能没有权限");
        }
    }

    /**
     * 批量添加群成员
     *
     * @param groupId 群组ID
     * @param params 参数，包含userIds
     * @param request HTTP请求
     * @return 添加结果
     */
    @PostMapping("/{groupId}/members")
    public Result addGroupMembers(
            @PathVariable Long groupId,
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        Long operatorId = AuthUtil.getCurrentUserId(request);
        if (operatorId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "操作者ID不能为空");
        }

        List<Long> userIds = (List<Long>) params.get("userIds");

        boolean success = chatGroupService.addGroupMembers(groupId, userIds, operatorId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("批量添加群成员失败，可能没有权限");
        }
    }

    /**
     * 移除群成员
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param request HTTP请求
     * @return 移除结果
     */
    @DeleteMapping("/{groupId}/member/{userId}")
    public Result removeGroupMember(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            HttpServletRequest request) {
        Long operatorId = AuthUtil.getCurrentUserId(request);
        if (operatorId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "操作者ID不能为空");
        }

        boolean success = chatGroupService.removeGroupMember(groupId, userId, operatorId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("移除群成员失败，可能没有权限");
        }
    }

    /**
     * 退出群组
     *
     * @param groupId 群组ID
     * @param request HTTP请求
     * @return 退出结果
     */
    @PostMapping("/{groupId}/quit")
    public Result quitGroup(@PathVariable Long groupId, HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        boolean success = chatGroupService.quitGroup(groupId, userId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("退出群组失败");
        }
    }

    /**
     * 设置群成员角色
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param params 参数，包含role
     * @param request HTTP请求
     * @return 设置结果
     */
    @PutMapping("/{groupId}/member/{userId}/role")
    public Result setGroupMemberRole(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        Long operatorId = AuthUtil.getCurrentUserId(request);
        if (operatorId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "操作者ID不能为空");
        }

        Integer role = Integer.valueOf(params.get("role").toString());

        boolean success = chatGroupService.setGroupMemberRole(groupId, userId, role, operatorId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("设置角色失败，可能没有权限");
        }
    }

    /**
     * 设置群成员昵称
     *
     * @param groupId 群组ID
     * @param params 参数，包含nickname
     * @param request HTTP请求
     * @return 设置结果
     */
    @PutMapping("/{groupId}/nickname")
    public Result setGroupMemberNickname(
            @PathVariable Long groupId,
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        String nickname = params.get("nickname").toString();

        boolean success = chatGroupService.setGroupMemberNickname(groupId, userId, nickname);
        if (success) {
            return Result.success();
        } else {
            return Result.error("设置群内昵称失败");
        }
    }

    /**
     * 禁言群成员
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param params 参数，包含muteMinutes
     * @param request HTTP请求
     * @return 禁言结果
     */
    @PutMapping("/{groupId}/member/{userId}/mute")
    public Result muteGroupMember(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        Long operatorId = AuthUtil.getCurrentUserId(request);
        if (operatorId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "操作者ID不能为空");
        }

        Integer muteMinutes = Integer.valueOf(params.get("muteMinutes").toString());
        LocalDateTime muteEndTime = LocalDateTime.now().plusMinutes(muteMinutes);

        boolean success = chatGroupService.muteGroupMember(groupId, userId, muteEndTime, operatorId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("禁言失败，可能没有权限");
        }
    }

    /**
     * 取消禁言
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param request HTTP请求
     * @return 取消禁言结果
     */
    @PutMapping("/{groupId}/member/{userId}/unmute")
    public Result unmuteGroupMember(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            HttpServletRequest request) {
        Long operatorId = AuthUtil.getCurrentUserId(request);
        if (operatorId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "操作者ID不能为空");
        }

        boolean success = chatGroupService.unmuteGroupMember(groupId, userId, operatorId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("取消禁言失败，可能没有权限");
        }
    }

    /**
     * 获取群成员列表
     *
     * @param groupId 群组ID
     * @return 群成员列表
     */
    @GetMapping("/{groupId}/members")
    public Result getGroupMembers(@PathVariable Long groupId) {
        List<GroupMember> members = chatGroupService.getGroupMembers(groupId);
        return Result.success(members);
    }

    /**
     * 获取群成员用户信息列表
     *
     * @param groupId 群组ID
     * @return 用户信息列表
     */
    @GetMapping("/{groupId}/users")
    public Result getGroupMemberUsers(@PathVariable Long groupId) {
        List<User> users = chatGroupService.getGroupMemberUsers(groupId);
        return Result.success(users);
    }

    /**
     * 获取用户的群组列表
     *
     * @param request HTTP请求
     * @return 群组列表
     */
    @GetMapping("/my")
    public Result getUserGroups(HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        List<ChatGroup> groups = chatGroupService.getUserGroups(userId);
        return Result.success(groups);
    }

    /**
     * 检查用户是否在群组中
     *
     * @param groupId 群组ID
     * @param request HTTP请求
     * @return 是否在群组中
     */
    @GetMapping("/{groupId}/check")
    public Result checkUserInGroup(@PathVariable Long groupId, HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        boolean isInGroup = chatGroupService.isUserInGroup(groupId, userId);
        return Result.success(isInGroup);
    }
}