package com.campus.im.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 消息实体类
 */
@Data
public class Message {
    
    /**
     * 消息ID
     */
    private Long id;
    
    /**
     * 会话类型：0-私聊，1-群聊
     */
    private Integer conversationType;
    
    /**
     * 发送者ID
     */
    private Long senderId;
    
    /**
     * 接收者ID：私聊为用户ID，群聊为群组ID
     */
    private Long receiverId;
    
    /**
     * 内容类型：0-文本，1-图片，2-语音，3-视频，4-文件，5-位置，99-系统消息
     */
    private Integer contentType;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 附加信息，JSON格式
     */
    private String extra;
    
    /**
     * 是否已撤回：0-否，1-是
     */
    private Integer isRecalled;
    
    /**
     * 状态：0-正常，1-已删除
     */
    private Integer status;
    
    /**
     * 发送时间
     */
    private LocalDateTime sendTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 