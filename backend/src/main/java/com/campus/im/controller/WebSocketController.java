package com.campus.im.controller;

import com.campus.im.common.constant.JwtConstant;
import com.campus.im.entity.Message;
import com.campus.im.entity.GroupMember;
import com.campus.im.service.MessageService;
import com.campus.im.service.UserService;
import com.campus.im.service.MentionService;
import com.campus.im.service.ChatGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * WebSocket控制器
 * 处理WebSocket消息
 */
@Controller
public class WebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private MentionService mentionService;

    @Autowired
    private ChatGroupService chatGroupService;

    /**
     * 处理私聊消息
     *
     * @param payload 消息内容
     * @param headerAccessor 消息头访问器
     */
    @MessageMapping("/private.message")
    public void handlePrivateMessage(@Payload Map<String, Object> payload,
                                    SimpMessageHeaderAccessor headerAccessor) {
        try {
            // 从会话中获取发送者手机号
            String senderPhone = getPhoneFromSession(headerAccessor);
            if (senderPhone == null) {
                logger.warn("发送者手机号为空");
                return;
            }

            // 获取发送者ID
            Long senderId = userService.getUserByPhone(senderPhone).getId();
            if (senderId == null) {
                logger.warn("发送者ID为空");
                return;
            }

            // 检查必要的参数是否存在
            if (payload.get("receiverId") == null) {
                logger.warn("接收者ID为空");
                return;
            }
            if (payload.get("contentType") == null) {
                logger.warn("内容类型为空");
                return;
            }
            if (payload.get("content") == null) {
                logger.warn("消息内容为空");
                return;
            }

            // 获取接收者ID
            Long receiverId = Long.valueOf(payload.get("receiverId").toString());
            Integer contentType = Integer.valueOf(payload.get("contentType").toString());
            String content = payload.get("content").toString();
            String extra = payload.get("extra") != null ? payload.get("extra").toString() : null;

            // 发送消息
            Message message = messageService.sendPrivateMessage(senderId, receiverId, contentType, content, extra);
            if (message != null) {
                // 转换消息为前端期望的格式
                Map<String, Object> convertedMessage = convertMessageToFrontendFormat(message);
                
                // 将消息发送给接收者
                String receiverPhone = userService.getUserById(receiverId).getPhone();
                logger.info("发送私聊消息给接收者: {}, 消息ID: {}", receiverPhone, message.getId());
                messagingTemplate.convertAndSendToUser(receiverPhone, "/queue/private.message", convertedMessage);
                
                // 将消息发送给发送者（确认消息已发送）
                logger.info("发送私聊消息给发送者: {}, 消息ID: {}", senderPhone, message.getId());
                messagingTemplate.convertAndSendToUser(senderPhone, "/queue/private.message", convertedMessage);
            }
        } catch (Exception e) {
            logger.error("处理私聊消息失败", e);
        }
    }

    /**
     * 处理群聊消息
     *
     * @param payload 消息内容
     * @param headerAccessor 消息头访问器
     */
    @MessageMapping("/group.message")
    public void handleGroupMessage(@Payload Map<String, Object> payload,
                                  SimpMessageHeaderAccessor headerAccessor) {
        try {
            // 从会话中获取发送者手机号
            String senderPhone = getPhoneFromSession(headerAccessor);
            if (senderPhone == null) {
                logger.warn("发送者手机号为空");
                return;
            }

            // 获取发送者ID
            Long senderId = userService.getUserByPhone(senderPhone).getId();
            if (senderId == null) {
                logger.warn("发送者ID为空");
                return;
            }

            // 检查必要的参数是否存在
            if (payload.get("groupId") == null) {
                logger.warn("群组ID为空");
                return;
            }
            if (payload.get("contentType") == null) {
                logger.warn("内容类型为空");
                return;
            }
            if (payload.get("content") == null) {
                logger.warn("消息内容为空");
                return;
            }

            // 获取群组ID
            Long groupId = Long.valueOf(payload.get("groupId").toString());
            Integer contentType = Integer.valueOf(payload.get("contentType").toString());
            String content = payload.get("content").toString();
            String extra = payload.get("extra") != null ? payload.get("extra").toString() : null;
            
            // 处理@功能
            @SuppressWarnings("unchecked")
            List<Long> mentionedUserIds = (List<Long>) payload.get("mentionedUserIds");

            // 发送消息
            Message message = messageService.sendGroupMessage(senderId, groupId, contentType, content, extra);
            if (message != null) {
                // 转换消息为前端期望的格式
                Map<String, Object> convertedMessage = convertMessageToFrontendFormat(message);
                
                // 将消息发送到群组频道
                logger.info("发送群聊消息到群组: {}, 消息ID: {}", groupId, message.getId());
                messagingTemplate.convertAndSend("/topic/group." + groupId, convertedMessage);
                
                // 同时发送到每个群成员的私有队列
                // 获取群组成员列表
                List<GroupMember> groupMembers = chatGroupService.getGroupMembers(groupId);
                for (GroupMember member : groupMembers) {
                    Long memberId = member.getUserId();
                    String memberPhone = userService.getUserById(memberId).getPhone();
                    logger.info("发送群聊消息给成员: {}, 消息ID: {}", memberPhone, message.getId());
                    messagingTemplate.convertAndSendToUser(memberPhone, "/queue/group.message", convertedMessage);
                }
                
                // 处理@提及
                if (mentionedUserIds != null && !mentionedUserIds.isEmpty()) {
                    mentionService.processMentions(message.getId(), groupId, content, mentionedUserIds);
                    
                    // 向被@的用户发送通知
                    for (Long mentionedUserId : mentionedUserIds) {
                        String mentionedUserPhone = userService.getUserById(mentionedUserId).getPhone();
                        messagingTemplate.convertAndSendToUser(mentionedUserPhone, "/queue/mentions", convertedMessage);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("处理群聊消息失败", e);
        }
    }

    /**
     * 处理消息已读回执
     *
     * @param payload 消息内容
     * @param headerAccessor 消息头访问器
     */
    @MessageMapping("/message.read")
    public void handleMessageRead(@Payload Map<String, Object> payload,
                                 SimpMessageHeaderAccessor headerAccessor) {
        try {
            // 从会话中获取发送者手机号
            String readerPhone = getPhoneFromSession(headerAccessor);
            if (readerPhone == null) {
                logger.warn("读者手机号为空");
                return;
            }

            // 检查必要的参数是否存在
            if (payload.get("messageId") == null) {
                logger.warn("消息ID为空");
                return;
            }

            // 获取消息ID
            Long messageId = Long.valueOf(payload.get("messageId").toString());

            // 标记消息为已读
            boolean success = messageService.markMessageAsRead(messageId, readerPhone);
            if (success) {
                // 获取消息详情
                Message message = messageService.getMessage(messageId);
                if (message != null && message.getSenderId() != null) {
                    // 获取发送者手机号
                    String senderPhone = userService.getUserById(message.getSenderId()).getPhone();
                    
                    // 将已读状态发送给消息发送者
                    messagingTemplate.convertAndSendToUser(senderPhone, "/queue/read-receipts", payload);
                }
            }
        } catch (Exception e) {
            logger.error("处理消息已读回执失败", e);
        }
    }

    /**
     * 处理用户在线状态
     *
     * @param payload 消息内容
     * @param headerAccessor 消息头访问器
     */
    @MessageMapping("/user.status")
    public void handleUserStatus(@Payload Map<String, Object> payload,
                                SimpMessageHeaderAccessor headerAccessor) {
        try {
            // 从会话中获取用户手机号
            String userPhone = getPhoneFromSession(headerAccessor);
            if (userPhone == null) {
                return;
            }

            // 更新用户在线状态
            userService.updateUserOnlineStatus(userPhone);
            
            // 通知该用户的好友其在线状态变化
            // 这里可以实现通知好友的逻辑
        } catch (Exception e) {
            logger.error("处理用户在线状态失败", e);
        }
    }

    /**
     * 处理消息撤回
     *
     * @param payload 消息内容
     * @param headerAccessor 消息头访问器
     */
    @MessageMapping("/message.recall")
    public void handleMessageRecall(@Payload Map<String, Object> payload,
                                   SimpMessageHeaderAccessor headerAccessor) {
        try {
            // 从会话中获取操作者手机号
            String operatorPhone = getPhoneFromSession(headerAccessor);
            if (operatorPhone == null) {
                return;
            }

            // 获取消息ID
            Long messageId = Long.valueOf(payload.get("messageId").toString());

            // 撤回消息
            boolean success = messageService.recallMessage(messageId, operatorPhone);
            if (success) {
                // 获取消息详情
                Message message = messageService.getMessage(messageId);
                if (message != null) {
                    // 获取会话类型
                    Integer conversationType = Integer.valueOf(payload.get("conversationType").toString());
                    
                    if (conversationType == 0) {
                        // 私聊消息
                        // 通知接收者消息已撤回
                        Long receiverId = Long.valueOf(payload.get("receiverId").toString());
                        String receiverPhone = userService.getUserById(receiverId).getPhone();
                        messagingTemplate.convertAndSendToUser(receiverPhone, "/queue/recall", payload);
                        
                        // 通知发送者消息已撤回
                        String senderPhone = userService.getUserById(message.getSenderId()).getPhone();
                        messagingTemplate.convertAndSendToUser(senderPhone, "/queue/recall", payload);
                    } else if (conversationType == 1) {
                        // 群聊消息
                        // 通知群组消息已撤回
                        Long groupId = Long.valueOf(payload.get("groupId").toString());
                        messagingTemplate.convertAndSend("/topic/group." + groupId + ".recall", payload);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("处理消息撤回失败", e);
        }
    }

    /**
     * 处理用户输入状态
     *
     * @param payload 消息内容
     * @param headerAccessor 消息头访问器
     */
    @MessageMapping("/user.typing")
    public void handleUserTyping(@Payload Map<String, Object> payload,
                                SimpMessageHeaderAccessor headerAccessor) {
        try {
            // 从会话中获取用户手机号
            String userPhone = getPhoneFromSession(headerAccessor);
            if (userPhone == null) {
                return;
            }

            // 获取接收者ID或群组ID
            String receiverPhone = payload.get("receiverPhone").toString();
            Boolean isTyping = Boolean.valueOf(payload.get("isTyping").toString());
            Integer conversationType = Integer.valueOf(payload.get("conversationType").toString());

            // 构建输入状态消息
            Map<String, Object> typingStatus = Map.of(
                "senderPhone", userPhone,
                "isTyping", isTyping
            );

            if (conversationType == 0) {
                // 私聊
                messagingTemplate.convertAndSendToUser(receiverPhone, "/queue/typing", typingStatus);
            } else if (conversationType == 1) {
                // 群聊
                Long groupId = Long.valueOf(payload.get("groupId").toString());
                messagingTemplate.convertAndSend("/topic/group." + groupId + ".typing", typingStatus);
            }
        } catch (Exception e) {
            logger.error("处理用户输入状态失败", e);
        }
    }

    /**
     * 从会话中获取用户手机号
     *
     * @param headerAccessor 消息头访问器
     * @return 用户手机号
     */
    private String getPhoneFromSession(SimpMessageHeaderAccessor headerAccessor) {
        if (headerAccessor != null && headerAccessor.getSessionAttributes() != null) {
            return (String) headerAccessor.getSessionAttributes().get(JwtConstant.PHONE_KEY);
        }
        return null;
    }

    /**
     * 将消息转换为前端期望的格式
     * 
     * @param message 消息实体
     * @return 转换后的消息
     */
    private Map<String, Object> convertMessageToFrontendFormat(Message message) {
        Map<String, Object> result = new HashMap<>();
        
        // 字段名称映射
        result.put("messageId", message.getId()); // id -> messageId
        
        // 设置会话ID
        if (message.getConversationType() == 0) {
            // 私聊消息，会话ID为两个用户ID的组合
            result.put("conversationId", createPrivateConversationId(message.getSenderId(), message.getReceiverId()));
        } else {
            // 群聊消息，会话ID为群组ID
            result.put("conversationId", message.getReceiverId());
        }
        
        result.put("senderId", message.getSenderId());
        result.put("receiverId", message.getReceiverId());
        result.put("contentType", message.getContentType());
        result.put("content", message.getContent());
        result.put("extra", message.getExtra());
        result.put("isRecalled", message.getIsRecalled() == 1);
        result.put("isRead", message.getStatus() == 1);
        result.put("timestamp", message.getSendTime().toString()); // sendTime -> timestamp
        result.put("sender", message.getSenderId()); // 兼容前端代码
        
        return result;
    }
    
    /**
     * 创建私聊会话ID（确保两个用户之间的会话ID一致）
     *
     * @param userId1 用户1的ID
     * @param userId2 用户2的ID
     * @return 会话ID
     */
    private Long createPrivateConversationId(Long userId1, Long userId2) {
        // 使用较小的ID作为前缀，确保两个用户之间的会话ID一致
        Long smallerId = Math.min(userId1, userId2);
        Long largerId = Math.max(userId1, userId2);
        
        // 简单的组合方式，可以根据实际需求调整
        return Long.parseLong(smallerId + "" + largerId);
    }
}