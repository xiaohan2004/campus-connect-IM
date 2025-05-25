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
     * 注册STOMP端点
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                                 WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
                        if (request instanceof ServletServerHttpRequest) {
                            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
                            // 从请求参数中获取token
                            String token = servletRequest.getServletRequest().getParameter("token");
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
                            }
                            return false;
                        }
                        return true;
                    }

                    @Override
                    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                             WebSocketHandler wsHandler, Exception exception) {
                        // 握手后的操作
                    }
                })
                .withSockJS(); // 启用SockJS支持
    }

    /**
     * 配置客户端入站通道拦截器
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            // 可以在这里添加消息处理的拦截器
        });
    }
} 