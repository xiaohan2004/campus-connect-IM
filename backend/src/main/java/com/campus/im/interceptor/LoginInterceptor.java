package com.campus.im.interceptor;

import com.campus.im.common.constant.JwtConstant;
import com.campus.im.common.Result;
import com.campus.im.common.enumeration.ResultCode;
import com.campus.im.entity.User;
import com.campus.im.service.UserService;
import com.campus.im.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

/**
 * JWT认证拦截器
 */
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;
    
    private ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 在请求处理之前进行调用
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求头中获取token
        String token = getTokenFromRequest(request);
        
        // 验证token
        if (StringUtils.hasText(token)) {
            try {
                // 使用parseJWT方法解析token，如果解析成功则表示token有效
                Claims claims = JwtUtil.parseJWT(token);
                
                // 提取JWT中的用户信息
                if (claims.get(JwtConstant.PHONE_KEY) != null) {
                    String phone = claims.get(JwtConstant.PHONE_KEY).toString();
                    request.setAttribute(JwtConstant.PHONE_KEY, phone);
                    
                    // 检查用户是否已存在，如果不存在则自动注册
                    autoRegisterUserIfNotExists(claims);
                } else {
                    // 如果JWT中没有phone字段，则无法识别用户
                    setResponse(response, Result.error(ResultCode.UNAUTHORIZED, "JWT中缺少phone信息"));
                    return false;
                }
                
                // 继续提取其他用户信息
                if (claims.get(JwtConstant.UID_KEY) != null) {
                    String uid = claims.get(JwtConstant.UID_KEY).toString();
                    request.setAttribute(JwtConstant.UID_KEY, uid);
                }
                
                if (claims.get(JwtConstant.NICKNAME_KEY) != null) {
                    String nickname = claims.get(JwtConstant.NICKNAME_KEY).toString();
                    request.setAttribute(JwtConstant.NICKNAME_KEY, nickname);
                }
                
                return true;
            } catch (ExpiredJwtException | MalformedJwtException e) {
                // token过期或格式错误
                setResponse(response, Result.error(ResultCode.UNAUTHORIZED, "登录已过期或token无效"));
                return false;
            } catch (Exception e) {
                // 其他验证失败情况
                setResponse(response, Result.error(ResultCode.UNAUTHORIZED, "token验证失败"));
                return false;
            }
        }
        
        // token为空，返回未授权错误
        setResponse(response, Result.error(ResultCode.UNAUTHORIZED, "未登录"));
        return false;
    }
    
    /**
     * 自动注册用户如果用户不存在
     * @param claims JWT中的声明信息
     */
    private void autoRegisterUserIfNotExists(Claims claims) {
        String phone = claims.get(JwtConstant.PHONE_KEY).toString();
        
        // 通过手机号查询用户是否存在
        User existingUser = userService.getUserByPhone(phone);
        
        if (existingUser == null) {
            // 用户不存在，创建新用户
            User newUser = new User();
            newUser.setPhone(phone);
            
            // 设置其他可能从JWT中获取的信息
            if (claims.get(JwtConstant.NICKNAME_KEY) != null) {
                newUser.setNickname(claims.get(JwtConstant.NICKNAME_KEY).toString());
            } else {
                // 如果没有昵称，可以使用默认昵称
                newUser.setNickname("用户" + phone.substring(Math.max(0, phone.length() - 4)));
            }
            
            // 设置用户状态为正常
            newUser.setStatus(0);
            
            // 设置创建和更新时间
            LocalDateTime now = LocalDateTime.now();
            newUser.setCreatedAt(now);
            newUser.setUpdatedAt(now);
            newUser.setLastActiveTime(now);
            
            // 保存用户到数据库
            userService.createUser(newUser);
        }
    }
    
    /**
     * 从请求头中获取token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(JwtConstant.TOKEN_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(JwtConstant.TOKEN_PREFIX)) {
            return bearerToken.substring(JwtConstant.TOKEN_PREFIX.length());
        }
        return null;
    }
    
    /**
     * 设置响应信息
     */
    private void setResponse(HttpServletResponse response, Result result) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter writer = response.getWriter();
        writer.write(objectMapper.writeValueAsString(result));
        writer.flush();
        writer.close();
    }
} 