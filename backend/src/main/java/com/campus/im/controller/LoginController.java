package com.campus.im.controller;

import com.campus.im.common.constant.JwtConstant;
import com.campus.im.common.Result;
import com.campus.im.common.enumeration.ResultCode;
import com.campus.im.dto.RegisterDTO;
import com.campus.im.dto.ResetPasswordDTO;
import com.campus.im.entity.User;
import com.campus.im.service.UserService;
import com.campus.im.util.AIChat;
import com.campus.im.util.EmailUtil;
import com.campus.im.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private EmailUtil emailUtil;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private AIChat aiChat;

    @Value("${app.url.login}")
    private String externalLoginUrl;
    @Value("${app.url.register}")
    private String externalRegisterUrl;
    @Value("${app.url.change-password}")
    private String externalChangePasswordUrl;
    @Value("${app.url.get-goods-by-phone}")
    private String externalGetGoodsByPhoneUrl;
    @Value("${app.url.goodspage}")
    private String externalGoodsPageUrl;

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
            ResponseEntity<Map> response = restTemplate.postForEntity(externalLoginUrl, requestEntity, Map.class);
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

    @PostMapping("/register")
    public Result register(@RequestBody RegisterDTO registerDTO) {
        // 参数校验
        if (registerDTO == null
                || !StringUtils.hasText(registerDTO.getPhone())
                || !StringUtils.hasText(registerDTO.getPassword())
                || !StringUtils.hasText(registerDTO.getEmail())
                || !StringUtils.hasText(registerDTO.getNickname())
                || registerDTO.getUserType() == null) {
            return Result.error(ResultCode.PARAM_ERROR, "注册信息不完整");
        }

        // 检查手机号是否已注册
        User existingUser = userService.getUserByPhone(registerDTO.getPhone());
        if (existingUser != null) {
            return Result.error(ResultCode.ERROR, "手机号已被注册");
        }

        // 检查验证码是否正确
        String redisKey = "email:code:register:" + registerDTO.getEmail();
        String cachedCode = stringRedisTemplate.opsForValue().get(redisKey);
        if (cachedCode == null || !cachedCode.equals(registerDTO.getVerificationCode())) {
            return Result.error(ResultCode.PARAM_ERROR, "验证码错误或已过期");
        }

        // 请求外部API
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("permission", registerDTO.getUserType());
        requestBody.put("phone", registerDTO.getPhone());
        requestBody.put("password", registerDTO.getPassword());
        requestBody.put("wallet", 0);
        requestBody.put("nickname", registerDTO.getNickname());
        requestBody.put("email", registerDTO.getEmail());
        requestBody.put("gender",0);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(externalRegisterUrl, requestEntity, Map.class);
        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !responseBody.get("msg").equals("success")) {
            return Result.error(ResultCode.ERROR, "外部注册服务失败");
        }

        // 创建新用户
        User newUser = new User();
        newUser.setPhone(registerDTO.getPhone());
        newUser.setNickname(registerDTO.getNickname());
        newUser.setStatus(0); // 正常状态

        LocalDateTime now = LocalDateTime.now();
        newUser.setCreatedAt(now);
        newUser.setUpdatedAt(now);
        newUser.setLastActiveTime(now);

        boolean success = userService.createUser(newUser);
        if (!success) {
            return Result.error("用户注册失败");
        }

        return Result.success("注册成功");
    }

    @PostMapping("/resetPassword")
    public Result resetPassword(@RequestBody ResetPasswordDTO resetPasswordDTO) {
        // 参数校验
        if (resetPasswordDTO == null
                || !StringUtils.hasText(resetPasswordDTO.getPhone())
                || !StringUtils.hasText(resetPasswordDTO.getEmail())
                || !StringUtils.hasText(resetPasswordDTO.getVerificationCode())
                || !StringUtils.hasText(resetPasswordDTO.getNewPassword())) {
            return Result.error(ResultCode.PARAM_ERROR, "重置密码信息不完整");
        }

        // 检查验证码是否正确
        String redisKey = "email:code:reset:" + resetPasswordDTO.getEmail();
        String cachedCode = stringRedisTemplate.opsForValue().get(redisKey);
        if (cachedCode == null || !cachedCode.equals(resetPasswordDTO.getVerificationCode())) {
            return Result.error(ResultCode.PARAM_ERROR, "验证码错误或已过期");
        }

        // 请求外部API重置密码
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("phone", resetPasswordDTO.getPhone());
        requestBody.put("password", resetPasswordDTO.getNewPassword());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(externalChangePasswordUrl, requestEntity, Map.class);
        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !responseBody.get("msg").equals("success")) {
            return Result.error(ResultCode.ERROR, "外部服务重置密码失败");
        }

        return Result.success("密码重置成功");
    }

    @PostMapping("/sendVerificationCode")
    public Result sendVerificationCode(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        String type = params.get("type");

        // 1. 生成并发送验证码
        String code = emailUtil.sendVerificationCode(email, type);

        // 2. 构造 Redis key（建议加上类型区分）
        String redisKey = "email:code:" + type + ":" + email;

        // 3. 缓存到 Redis，有效期 3 分钟
        stringRedisTemplate.opsForValue().set(redisKey, code, 3, TimeUnit.MINUTES);

        return Result.success("验证码已发送");
    }

    @GetMapping("/getGoodsByPhone")
    public Result getGoodsByPhone(@RequestParam String phone) {
        // 参数校验
        if (!StringUtils.hasText(phone)) {
            return Result.error(ResultCode.PARAM_ERROR, "手机号不能为空");
        }

        // 请求外部API获取商品信息
        String url = externalGetGoodsByPhoneUrl + '/' + phone;
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !responseBody.get("msg").equals("success")) {
            return Result.error(ResultCode.ERROR, "获取商品信息失败");
        }

        // 返回商品数据
        return Result.success(responseBody.get("data"));
    }

    @PostMapping("/aiAD")
    public Result aiAD(@RequestBody Map<String, String> params) {
        String gid = params.get("gid");
        String name = params.get("name");
        String intro = params.get("intro");

        String ad = aiChat.getResponse(name, intro);
        if (ad == null || ad.isEmpty()) {
            return Result.error(ResultCode.ERROR, "AI生成广告文案失败");
        }

        ad += "<br><br>👉 <a href='"+ externalGoodsPageUrl + '/' + gid + "' target='_blank'>点击查看详情</a>";

        return Result.success(ad);
    }
}