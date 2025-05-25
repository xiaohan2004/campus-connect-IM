package com.campus.im.controller;

import com.campus.im.common.constant.JwtConstant;
import com.campus.im.entity.Result;
import com.campus.im.entity.ResultCode;
import com.campus.im.entity.User;
import com.campus.im.service.UserService;
import com.campus.im.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录控制器
 */
@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${app.login.url}")
    private String externalAuthApiUrl;

    /**
     * 用户登录
     *
     * @param params 登录参数，包含phone和password
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String password = params.get("password");

        // 参数校验
        if (!StringUtils.hasText(phone) || !StringUtils.hasText(password)) {
            return Result.error(ResultCode.PARAM_ERROR, "手机号和密码不能为空");
        }

        try {
            // 构建请求外部认证API的参数
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("phone", phone);
            requestBody.put("password", password);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

            // 发送请求到外部认证API
            ResponseEntity<Map> response = restTemplate.postForEntity(externalAuthApiUrl, requestEntity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            // 验证外部API返回的结果
            if (responseBody == null || responseBody.get("data") == null) {
                return Result.error(ResultCode.UNAUTHORIZED, "认证失败");
            }

            String externalToken = responseBody.get("data").toString();

            // 验证外部Token
            Claims claims;
            try {
                claims = JwtUtil.parseJWT(externalToken);
                if (claims == null || claims.get("phone") == null) {
                    return Result.error(ResultCode.UNAUTHORIZED, "无效的认证令牌");
                }
            } catch (ExpiredJwtException | MalformedJwtException e) {
                return Result.error(ResultCode.UNAUTHORIZED, "令牌已过期或格式错误");
            } catch (Exception e) {
                return Result.error(ResultCode.UNAUTHORIZED, "无效的认证令牌");
            }

            // 获取用户信息
            String userPhone = claims.get("phone").toString();
            User user = userService.getUserByPhone(userPhone);

            // 如果用户不存在，则自动注册
            if (user == null) {
                user = new User();
                user.setPhone(userPhone);
                user.setNickname("用户" + userPhone.substring(Math.max(0, userPhone.length() - 4)));
                user.setStatus(0); // 正常状态

                LocalDateTime now = LocalDateTime.now();
                user.setCreatedAt(now);
                user.setUpdatedAt(now);
                user.setLastActiveTime(now);

                // 如果外部Token中包含其他用户信息，可以设置到user对象中
                if (claims.get("nickname") != null) {
                    user.setNickname(claims.get("nickname").toString());
                }

                // 保存用户到数据库
                boolean success = userService.createUser(user);
                if (!success) {
                    return Result.error("用户注册失败");
                }

                // 重新获取用户信息（获取自动生成的ID）
                user = userService.getUserByPhone(userPhone);
            }

            // 更新用户最后活跃时间
            user.setLastActiveTime(LocalDateTime.now());
            userService.updateUser(user);

            // 生成新的JWT令牌
            Map<String, Object> tokenClaims = new HashMap<>();
            tokenClaims.put(JwtConstant.PHONE_KEY, user.getPhone());
            tokenClaims.put(JwtConstant.UID_KEY, user.getId().toString());
            tokenClaims.put(JwtConstant.NICKNAME_KEY, user.getNickname());

            String token = JwtUtil.generateJwt(tokenClaims);

            return Result.success(token);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(ResultCode.ERROR, "登录失败: " + e.getMessage());
        }
    }
}