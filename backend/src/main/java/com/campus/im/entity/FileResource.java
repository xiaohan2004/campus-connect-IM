package com.campus.im.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文件资源实体类
 */
@Data
public class FileResource {
    
    /**
     * 资源ID
     */
    private Long id;
    
    /**
     * 上传用户ID
     */
    private Long userId;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 文件类型
     */
    private String fileType;
    
    /**
     * 文件大小(字节)
     */
    private Long fileSize;
    
    /**
     * 文件URL
     */
    private String fileUrl;
    
    /**
     * 缩略图URL（图片和视频有）
     */
    private String thumbnailUrl;
    
    /**
     * 时长(毫秒)，音频和视频有
     */
    private Integer duration;
    
    /**
     * 宽度(像素)，图片和视频有
     */
    private Integer width;
    
    /**
     * 高度(像素)，图片和视频有
     */
    private Integer height;
    
    /**
     * 状态：0-正常，1-已删除
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 