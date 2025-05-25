package com.campus.im.controller;

import com.campus.im.entity.Result;
import com.campus.im.entity.ResultCode;
import com.campus.im.entity.User;
import com.campus.im.service.UserService;
import com.campus.im.util.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取当前登录用户信息
     *
     * @param request HTTP请求
     * @return 用户信息
     */
    @GetMapping("/current")
    public Result getCurrentUser(HttpServletRequest request) {
        String phone = AuthUtil.getCurrentUserPhone(request);
        User user = userService.getUserByPhone(phone);
        if (user == null) {
            return Result.error(ResultCode.NOT_FOUND, "用户不存在");
        }
        return Result.success(user);
    }

    /**
     * 根据手机号获取用户信息
     * 注意：只有当前登录用户才能获取自己的详细信息
     *
     * @param phone 手机号
     * @param request HTTP请求
     * @return 用户信息
     */
    @GetMapping("/phone/{phone}")
    public Result getUserByPhone(@PathVariable String phone, HttpServletRequest request) {
        // 检查是否有权限获取该用户信息
        Result permissionCheck = AuthUtil.checkPermission(request, phone);
        if (permissionCheck != null) {
            return permissionCheck;
        }

        User user = userService.getUserByPhone(phone);
        if (user == null) {
            return Result.error(ResultCode.NOT_FOUND, "用户不存在");
        }
        return Result.success(user);
    }

    /**
     * 根据ID获取用户信息
     * 注意：只有当前登录用户才能获取自己的详细信息
     *
     * @param id 用户ID
     * @param request HTTP请求
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public Result getUserById(@PathVariable Long id, HttpServletRequest request) {
        User user = userService.getUserById(id);
        if (user == null) {
            return Result.error(ResultCode.NOT_FOUND, "用户不存在");
        }

        // 检查是否有权限获取该用户信息
        Result permissionCheck = AuthUtil.checkPermission(request, user.getPhone());
        if (permissionCheck != null) {
            // 如果不是当前用户，只返回基本信息
            User basicUser = new User();
            basicUser.setId(user.getId());
            basicUser.setNickname(user.getNickname());
            basicUser.setAvatar(user.getAvatar());
            basicUser.setStatus(user.getStatus());
            return Result.success(basicUser);
        }

        return Result.success(user);
    }

    /**
     * 更新用户信息
     * 注意：只有当前登录用户才能更新自己的信息
     *
     * @param user 用户信息
     * @param request HTTP请求
     * @return 更新结果
     */
    @PutMapping
    public Result updateUser(@RequestBody User user, HttpServletRequest request) {
        // 检查是否有权限更新该用户信息
        Result permissionCheck = AuthUtil.checkPermission(request, user.getPhone());
        if (permissionCheck != null) {
            return permissionCheck;
        }

        // 获取当前用户
        User currentUser = userService.getUserByPhone(AuthUtil.getCurrentUserPhone(request));
        if (currentUser == null) {
            return Result.error(ResultCode.NOT_FOUND, "用户不存在");
        }

        // 设置不允许修改的字段
        user.setId(currentUser.getId());
        user.setPhone(currentUser.getPhone());
        user.setCreatedAt(currentUser.getCreatedAt());

        // 更新用户信息
        boolean success = userService.updateUser(user);
        if (success) {
            return Result.success();
        } else {
            return Result.error("更新失败");
        }
    }

    /**
     * 更新用户在线状态
     *
     * @param request HTTP请求
     * @return 更新结果
     */
    @PutMapping("/status")
    public Result updateUserStatus(HttpServletRequest request) {
        String phone = AuthUtil.getCurrentUserPhone(request);
        boolean success = userService.updateUserOnlineStatus(phone);
        if (success) {
            return Result.success();
        } else {
            return Result.error("更新状态失败");
        }
    }
} 