package com.campus.im.common.constant;

/**
 * 消息相关常量
 */
public class MessageConstant {
    
    /**
     * 会话类型：私聊
     */
    public static final int CONVERSATION_TYPE_PRIVATE = 0;
    
    /**
     * 会话类型：群聊
     */
    public static final int CONVERSATION_TYPE_GROUP = 1;
    
    /**
     * 消息内容类型：文本
     */
    public static final int CONTENT_TYPE_TEXT = 0;
    
    /**
     * 消息内容类型：图片
     */
    public static final int CONTENT_TYPE_IMAGE = 1;
    
    /**
     * 消息内容类型：语音
     */
    public static final int CONTENT_TYPE_VOICE = 2;
    
    /**
     * 消息内容类型：视频
     */
    public static final int CONTENT_TYPE_VIDEO = 3;
    
    /**
     * 消息内容类型：文件
     */
    public static final int CONTENT_TYPE_FILE = 4;
    
    /**
     * 消息内容类型：位置
     */
    public static final int CONTENT_TYPE_LOCATION = 5;
    
    /**
     * 消息内容类型：系统消息
     */
    public static final int CONTENT_TYPE_SYSTEM = 99;
    
    /**
     * 消息状态：正常
     */
    public static final int STATUS_NORMAL = 0;
    
    /**
     * 消息状态：已删除
     */
    public static final int STATUS_DELETED = 1;
    
    /**
     * 是否已撤回：否
     */
    public static final int IS_RECALLED_NO = 0;
    
    /**
     * 是否已撤回：是
     */
    public static final int IS_RECALLED_YES = 1;
} 