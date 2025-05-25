package com.campus.im.common.constant;

/**
 * JWT常量
 */
public class JwtConstant {
    
    /**
     * 请求头中的token名称
     */
    public static final String TOKEN_HEADER = "Authorization";
    
    /**
     * token前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";
    
    /**
     * token在Redis中的key前缀
     */
    public static final String TOKEN_KEY_PREFIX = "jwt:token:";
    
    /**
     * 用户uid的key
     */
    public static final String UID_KEY = "uid";
    
    /**
     * 用户手机号的key
     */
    public static final String PHONE_KEY = "phone";
    
    /**
     * 用户昵称的key
     */
    public static final String NICKNAME_KEY = "nickname";
    
    /**
     * token过期时间（毫秒）
     */
    public static final long EXPIRATION = 24 * 60 * 60 * 1000; // 24小时
    
    /**
     * 禁止实例化
     */
    private JwtConstant() {
    }
} 