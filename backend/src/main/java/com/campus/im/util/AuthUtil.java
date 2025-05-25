package com.campus.im.util;

import com.campus.im.common.constant.JwtConstant;
import com.campus.im.entity.Result;
import com.campus.im.entity.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * 权限检查工具类
 */
public class AuthUtil {

    /**
     * 获取当前登录用户的手机号
     *
     * @param request HTTP请求
     * @return 手机号
     */
    public static String getCurrentUserPhone(HttpServletRequest request) {
        return (String) request.getAttribute(JwtConstant.PHONE_KEY);
    }

    /**
     * 获取当前登录用户的ID
     *
     * @param request HTTP请求
     * @return 用户ID
     */
    public static Long getCurrentUserId(HttpServletRequest request) {
        String uidStr = (String) request.getAttribute(JwtConstant.UID_KEY);
        return StringUtils.hasText(uidStr) ? Long.valueOf(uidStr) : null;
    }

    /**
     * 获取当前登录用户的昵称
     *
     * @param request HTTP请求
     * @return 昵称
     */
    public static String getCurrentUserNickname(HttpServletRequest request) {
        return (String) request.getAttribute(JwtConstant.NICKNAME_KEY);
    }

    /**
     * 检查是否为当前用户的手机号
     *
     * @param request HTTP请求
     * @param phone 待检查的手机号
     * @return 是否为当前用户
     */
    public static boolean isCurrentUser(HttpServletRequest request, String phone) {
        String currentPhone = getCurrentUserPhone(request);
        return StringUtils.hasText(currentPhone) && currentPhone.equals(phone);
    }

    /**
     * 检查是否为当前用户的ID
     *
     * @param request HTTP请求
     * @param userId 待检查的用户ID
     * @return 是否为当前用户
     */
    public static boolean isCurrentUser(HttpServletRequest request, Long userId) {
        Long currentUserId = getCurrentUserId(request);
        return currentUserId != null && currentUserId.equals(userId);
    }

    /**
     * 检查权限并返回结果
     *
     * @param request HTTP请求
     * @param phone 待检查的手机号
     * @return 如果有权限则返回null，否则返回错误结果
     */
    public static Result checkPermission(HttpServletRequest request, String phone) {
        if (!isCurrentUser(request, phone)) {
            return Result.error(ResultCode.FORBIDDEN, "无权操作他人账号");
        }
        return null;
    }

    /**
     * 检查权限并返回结果
     *
     * @param request HTTP请求
     * @param userId 待检查的用户ID
     * @return 如果有权限则返回null，否则返回错误结果
     */
    public static Result checkPermission(HttpServletRequest request, Long userId) {
        if (!isCurrentUser(request, userId)) {
            return Result.error(ResultCode.FORBIDDEN, "无权操作他人账号");
        }
        return null;
    }
} 