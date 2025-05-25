package com.campus.im.service;

import com.campus.im.entity.FileResource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件资源服务接口
 */
public interface FileResourceService {
    
    /**
     * 上传文件
     *
     * @param file 文件
     * @param userId 用户ID
     * @return 文件资源
     */
    FileResource uploadFile(MultipartFile file, Long userId);
    
    /**
     * 上传图片
     *
     * @param file 图片文件
     * @param userId 用户ID
     * @return 文件资源
     */
    FileResource uploadImage(MultipartFile file, Long userId);
    
    /**
     * 上传语音
     *
     * @param file 语音文件
     * @param userId 用户ID
     * @param duration 时长(毫秒)
     * @return 文件资源
     */
    FileResource uploadVoice(MultipartFile file, Long userId, Integer duration);
    
    /**
     * 上传视频
     *
     * @param file 视频文件
     * @param userId 用户ID
     * @param duration 时长(毫秒)
     * @param width 宽度(像素)
     * @param height 高度(像素)
     * @return 文件资源
     */
    FileResource uploadVideo(MultipartFile file, Long userId, Integer duration, Integer width, Integer height);
    
    /**
     * 删除文件
     *
     * @param id 资源ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteFile(Long id, Long userId);
    
    /**
     * 获取文件资源
     *
     * @param id 资源ID
     * @return 文件资源
     */
    FileResource getFileResource(Long id);
    
    /**
     * 获取用户的文件资源列表
     *
     * @param userId 用户ID
     * @return 文件资源列表
     */
    List<FileResource> getUserFileResources(Long userId);
    
    /**
     * 获取用户的特定类型文件资源列表
     *
     * @param userId 用户ID
     * @param fileType 文件类型
     * @return 文件资源列表
     */
    List<FileResource> getUserFileResourcesByType(Long userId, String fileType);
    
    /**
     * 生成缩略图
     *
     * @param fileResource 文件资源
     * @return 是否成功
     */
    boolean generateThumbnail(FileResource fileResource);
} 