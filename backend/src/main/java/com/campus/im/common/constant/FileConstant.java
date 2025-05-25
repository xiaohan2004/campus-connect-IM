package com.campus.im.common.constant;

/**
 * 文件相关常量
 */
public class FileConstant {
    
    /**
     * 文件状态：正常
     */
    public static final int STATUS_NORMAL = 0;
    
    /**
     * 文件状态：已删除
     */
    public static final int STATUS_DELETED = 1;
    
    /**
     * 文件类型：图片
     */
    public static final String TYPE_IMAGE = "image";
    
    /**
     * 文件类型：语音
     */
    public static final String TYPE_VOICE = "voice";
    
    /**
     * 文件类型：视频
     */
    public static final String TYPE_VIDEO = "video";
    
    /**
     * 文件类型：文档
     */
    public static final String TYPE_DOCUMENT = "document";
    
    /**
     * 文件类型：其他
     */
    public static final String TYPE_OTHER = "other";
    
    /**
     * 允许的图片格式
     */
    public static final String[] ALLOWED_IMAGE_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "bmp", "webp"};
    
    /**
     * 允许的语音格式
     */
    public static final String[] ALLOWED_VOICE_EXTENSIONS = {"mp3", "wav", "aac", "ogg"};
    
    /**
     * 允许的视频格式
     */
    public static final String[] ALLOWED_VIDEO_EXTENSIONS = {"mp4", "avi", "mov", "wmv", "flv", "mkv"};
    
    /**
     * 允许的文档格式
     */
    public static final String[] ALLOWED_DOCUMENT_EXTENSIONS = {"doc", "docx", "xls", "xlsx", "ppt", "pptx", "pdf", "txt"};
    
    /**
     * 最大文件大小（字节）- 50MB
     */
    public static final long MAX_FILE_SIZE = 52428800L;
    
    /**
     * 最大图片大小（字节）- 10MB
     */
    public static final long MAX_IMAGE_SIZE = 10485760L;
    
    /**
     * 最大语音大小（字节）- 5MB
     */
    public static final long MAX_VOICE_SIZE = 5242880L;
    
    /**
     * 最大视频大小（字节）- 50MB
     */
    public static final long MAX_VIDEO_SIZE = 52428800L;
    
    /**
     * 默认图片缩略图宽度
     */
    public static final int DEFAULT_THUMBNAIL_WIDTH = 200;
    
    /**
     * 默认图片缩略图高度
     */
    public static final int DEFAULT_THUMBNAIL_HEIGHT = 200;
} 