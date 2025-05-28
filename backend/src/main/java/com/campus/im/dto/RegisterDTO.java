package com.campus.im.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册数据传输对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDTO {

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 用户身份
     */
    private Integer userType;

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
     * 密码
     */
    private String password;
}