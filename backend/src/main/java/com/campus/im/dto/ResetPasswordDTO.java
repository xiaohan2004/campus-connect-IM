package com.campus.im.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 重置密码数据传输对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordDTO {
    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 验证码
     */
    private String verificationCode;

    /**
     * 新密码
     */
    private String newPassword;
}