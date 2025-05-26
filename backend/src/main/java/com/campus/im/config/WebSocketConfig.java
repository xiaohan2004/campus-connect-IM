package com.campus.im.config;

import com.campus.im.common.constant.JwtConstant;
import com.campus.im.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket配置类
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    /**
     * 配置消息代理
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 客户端订阅消息的前缀
        registry.enableSimpleBroker("/topic", "/queue");
        // 客户端发送消息的前缀
        registry.setApplicationDestinationPrefixes("/app");
        // 用户消息前缀
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * 注册STOMP端点，只注册一次，支持SockJS，并复用拦截器
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        HandshakeInterceptor jwtInterceptor = new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                           WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
                if (request instanceof ServletServerHttpRequest servletRequest) {
                    // 先从请求头获取token
                    String authHeader = servletRequest.getServletRequest().getHeader("Authorization");
                    String token = null;

                    if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                        token = authHeader.substring(7); // 去掉"Bearer "前缀
                        logger.debug("从请求头获取到token: {}", token);
                    }

                    // 请求头没token，再从URL参数中获取（浏览器WebSocket无法设置header，推荐使用URL传token）
                    if (!StringUtils.hasText(token)) {
                        token = servletRequest.getServletRequest().getParameter("token");
                        logger.debug("从URL参数获取到token: {}", token);
                    }

                    if (StringUtils.hasText(token)) {
                        try {
                            // 验证token
                            Claims claims = JwtUtil.parseJWT(token);
                            if (claims.get(JwtConstant.PHONE_KEY) != null) {
                                String phone = claims.get(JwtConstant.PHONE_KEY).toString();
                                // 将手机号放入WebSocket会话属性中
                                attributes.put(JwtConstant.PHONE_KEY, phone);
                                return true;
                            }
                        } catch (Exception e) {
                            logger.error("WebSocket连接token验证失败", e);
                        }
                    } else {
                        logger.error("WebSocket连接缺少token");
                    }
                    return false;
                }
                return true;
            }

            @Override
            public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                       WebSocketHandler wsHandler, Exception exception) {
                // 握手完成后可以做些操作，当前不处理
            }
        };

        // 只注册一个 /ws 端点，启用SockJS，允许所有源
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(jwtInterceptor)
                .withSockJS();
    }

    /**
     * 配置客户端入站通道拦截器（可拓展）
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            // 这里可以添加消息处理拦截逻辑
        });
    }
}