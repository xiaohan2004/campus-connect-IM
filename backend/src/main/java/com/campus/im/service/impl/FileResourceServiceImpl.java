package com.campus.im.service.impl;

import com.campus.im.common.constant.FileConstant;
import com.campus.im.entity.FileResource;
import com.campus.im.mapper.FileResourceMapper;
import com.campus.im.service.FileResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 文件资源服务实现类
 */
@Service
public class FileResourceServiceImpl implements FileResourceService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileResourceServiceImpl.class);
    
    @Autowired
    private FileResourceMapper fileResourceMapper;
    
    @Value("${app.file.upload-dir}")
    private String uploadDir;
    
    @Override
    @Transactional
    public FileResource uploadFile(MultipartFile file, Long userId) {
        if (file == null || file.isEmpty() || userId == null) {
            return null;
        }
        
        try {
            // 检查文件大小
            if (file.getSize() > FileConstant.MAX_FILE_SIZE) {
                throw new IllegalArgumentException("文件大小超过限制");
            }
            
            // 创建上传目录
            String directory = "files/" + userId;
            String fileType = determineFileType(file);
            
            return saveFile(file, userId, directory, fileType);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    @Transactional
    public FileResource uploadImage(MultipartFile file, Long userId) {
        if (file == null || file.isEmpty() || userId == null) {
            return null;
        }
        
        try {
            // 检查文件大小
            if (file.getSize() > FileConstant.MAX_IMAGE_SIZE) {
                throw new IllegalArgumentException("图片大小超过限制");
            }
            
            // 检查是否为图片
            String extension = getFileExtension(file.getOriginalFilename());
            if (!Arrays.asList(FileConstant.ALLOWED_IMAGE_EXTENSIONS).contains(extension.toLowerCase())) {
                throw new IllegalArgumentException("不支持的图片格式");
            }
            
            // 创建上传目录
            String directory = "images/" + userId;
            
            FileResource fileResource = saveFile(file, userId, directory, FileConstant.TYPE_IMAGE);
            
            // 生成缩略图
            generateThumbnail(fileResource);
            
            return fileResource;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    @Transactional
    public FileResource uploadVoice(MultipartFile file, Long userId, Integer duration) {
        if (file == null || file.isEmpty() || userId == null) {
            return null;
        }
        
        try {
            // 检查文件大小
            if (file.getSize() > FileConstant.MAX_VOICE_SIZE) {
                throw new IllegalArgumentException("语音大小超过限制");
            }
            
            // 检查是否为语音
            String extension = getFileExtension(file.getOriginalFilename());
            if (!Arrays.asList(FileConstant.ALLOWED_VOICE_EXTENSIONS).contains(extension.toLowerCase())) {
                throw new IllegalArgumentException("不支持的语音格式");
            }
            
            // 创建上传目录
            String directory = "voices/" + userId;
            
            FileResource fileResource = saveFile(file, userId, directory, FileConstant.TYPE_VOICE);
            
            // 设置语音时长
            if (duration != null) {
                fileResource.setDuration(duration);
                fileResourceMapper.update(fileResource);
            }
            
            return fileResource;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    @Transactional
    public FileResource uploadVideo(MultipartFile file, Long userId, Integer duration, Integer width, Integer height) {
        if (file == null || file.isEmpty() || userId == null) {
            return null;
        }
        
        try {
            // 检查文件大小
            if (file.getSize() > FileConstant.MAX_VIDEO_SIZE) {
                throw new IllegalArgumentException("视频大小超过限制");
            }
            
            // 检查是否为视频
            String extension = getFileExtension(file.getOriginalFilename());
            if (!Arrays.asList(FileConstant.ALLOWED_VIDEO_EXTENSIONS).contains(extension.toLowerCase())) {
                throw new IllegalArgumentException("不支持的视频格式");
            }
            
            // 创建上传目录
            String directory = "videos/" + userId;
            
            FileResource fileResource = saveFile(file, userId, directory, FileConstant.TYPE_VIDEO);
            
            // 设置视频信息
            if (duration != null) {
                fileResource.setDuration(duration);
            }
            if (width != null) {
                fileResource.setWidth(width);
            }
            if (height != null) {
                fileResource.setHeight(height);
            }
            
            if (duration != null || width != null || height != null) {
                fileResourceMapper.update(fileResource);
            }
            
            // 生成视频缩略图
            try {
                boolean thumbnailResult = generateVideoThumbnail(fileResource);
                if (thumbnailResult) {
                    logger.info("视频缩略图生成成功: {}", fileResource.getId());
                } else {
                    logger.warn("视频缩略图生成失败: {}", fileResource.getId());
                }
            } catch (Exception e) {
                // 缩略图生成失败不影响视频上传
                logger.error("视频缩略图生成异常: {}", fileResource.getId(), e);
            }
            
            return fileResource;
        } catch (Exception e) {
            logger.error("视频上传失败: ", e);
            return null;
        }
    }
    
    @Override
    @Transactional
    public boolean deleteFile(Long id, Long userId) {
        if (id == null || userId == null) {
            return false;
        }
        
        // 检查文件是否存在且属于该用户
        FileResource fileResource = fileResourceMapper.selectById(id);
        if (fileResource == null || !fileResource.getUserId().equals(userId)) {
            return false;
        }
        
        // 逻辑删除文件记录
        return fileResourceMapper.delete(id) > 0;
    }
    
    @Override
    public FileResource getFileResource(Long id) {
        if (id == null) {
            return null;
        }
        
        return fileResourceMapper.selectById(id);
    }
    
    @Override
    public List<FileResource> getUserFileResources(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        
        return fileResourceMapper.selectByUserId(userId);
    }
    
    @Override
    public List<FileResource> getUserFileResourcesByType(Long userId, String fileType) {
        if (userId == null || fileType == null) {
            return Collections.emptyList();
        }
        
        return fileResourceMapper.selectByUserIdAndFileType(userId, fileType);
    }
    
    @Override
    public boolean generateThumbnail(FileResource fileResource) {
        if (fileResource == null || !FileConstant.TYPE_IMAGE.equals(fileResource.getFileType())) {
            return false;
        }
        
        try {
            // 原图路径
            Path imagePath = Paths.get(uploadDir, fileResource.getFileUrl());
            
            // 确保原图存在
            if (!Files.exists(imagePath)) {
                return false;
            }
            
            // 读取原图
            BufferedImage originalImage = ImageIO.read(imagePath.toFile());
            
            // 计算缩略图尺寸（保持比例）
            int width = FileConstant.DEFAULT_THUMBNAIL_WIDTH;
            int height = (int) (((double) originalImage.getHeight() / originalImage.getWidth()) * width);
            
            // 生成缩略图
            BufferedImage thumbnailImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = thumbnailImage.createGraphics();
            g2d.drawImage(originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
            g2d.dispose();
            
            // 保存缩略图
            String fileName = "thumbnail_" + getFileNameFromPath(fileResource.getFileUrl());
            String directory = Paths.get(fileResource.getFileUrl()).getParent().toString();
            Path thumbnailPath = Paths.get(uploadDir, directory, fileName);
            
            // 确保目录存在
            Files.createDirectories(thumbnailPath.getParent());
            
            // 保存缩略图
            ImageIO.write(thumbnailImage, getFileExtension(fileName), thumbnailPath.toFile());
            
            // 更新缩略图URL
            fileResource.setThumbnailUrl(Paths.get(directory, fileName).toString());
            fileResource.setWidth(originalImage.getWidth());
            fileResource.setHeight(originalImage.getHeight());
            
            // 更新数据库
            return fileResourceMapper.update(fileResource) > 0;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 生成视频缩略图
     * 注意：实际实现需要依赖FFmpeg等工具
     */
    private boolean generateVideoThumbnail(FileResource fileResource) {
        if (fileResource == null || !FileConstant.TYPE_VIDEO.equals(fileResource.getFileType())) {
            logger.warn("无效的文件资源或非视频类型，无法生成缩略图");
            return false;
        }
        
        try {
            // 在实际项目中，通常使用以下方式生成视频缩略图:
            // 1. 使用FFmpeg提取视频的第一帧或指定时间的帧
            // 2. 保存为图片文件
            
            String outputFileName = "thumbnail_" + getFileNameFromPath(fileResource.getFileUrl()).replace(".", "_") + ".jpg";
            String directory = Paths.get(fileResource.getFileUrl()).getParent().toString();
            String thumbnailPath = Paths.get(directory, outputFileName).toString();
            
            String inputFilePath = Paths.get(uploadDir, fileResource.getFileUrl()).toString();
            String outputFilePath = Paths.get(uploadDir, thumbnailPath).toString();
            
            // 确保输出目录存在
            Files.createDirectories(Paths.get(outputFilePath).getParent());
            
            // 检查FFmpeg是否可用
            try {
                Process checkProcess = new ProcessBuilder("ffmpeg", "-version").start();
                boolean exited = checkProcess.waitFor(5, TimeUnit.SECONDS);
                if (!exited || checkProcess.exitValue() != 0) {
                    logger.error("FFmpeg不可用，无法生成视频缩略图");
                    return false;
                }
            } catch (IOException e) {
                logger.error("FFmpeg命令不可用: {}", e.getMessage());
                return false;
            }
            
            logger.info("开始生成视频缩略图: {}", inputFilePath);
            
            // 使用ProcessBuilder调用FFmpeg
            ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg", "-i", inputFilePath, 
                "-ss", "00:00:01", // 从视频的1秒处截取
                "-vframes", "1",   // 截取1帧
                "-s", FileConstant.DEFAULT_THUMBNAIL_WIDTH + "x" + FileConstant.DEFAULT_THUMBNAIL_HEIGHT,
                "-y",              // 覆盖已存在的文件
                outputFilePath
            );
            
            // 将错误输出合并到标准输出
            processBuilder.redirectErrorStream(true);
            
            Process process = processBuilder.start();
            
            // 设置超时时间
            boolean completed = process.waitFor(30, TimeUnit.SECONDS);
            if (!completed) {
                logger.error("生成视频缩略图超时");
                process.destroyForcibly();
                return false;
            }
            
            int exitCode = process.exitValue();
            
            if (exitCode == 0) {
                // 检查缩略图是否真的生成了
                if (Files.exists(Paths.get(outputFilePath))) {
                    // 更新缩略图URL
                    fileResource.setThumbnailUrl(thumbnailPath);
                    return fileResourceMapper.update(fileResource) > 0;
                } else {
                    logger.error("FFmpeg执行成功但缩略图文件未生成: {}", outputFilePath);
                    return false;
                }
            } else {
                logger.error("视频缩略图生成失败，FFmpeg返回代码: {}", exitCode);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("生成视频缩略图时发生异常: ", e);
            return false;
        }
    }
    
    /**
     * 保存文件并创建数据库记录
     */
    private FileResource saveFile(MultipartFile file, Long userId, String directory, String fileType) throws IOException {
        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String fileName = UUID.randomUUID().toString() + "." + extension;
        
        // 创建目录
        Path directoryPath = Paths.get(uploadDir, directory);
        Files.createDirectories(directoryPath);
        
        // 保存文件
        Path filePath = directoryPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // 创建文件资源记录
        FileResource fileResource = new FileResource();
        fileResource.setUserId(userId);
        fileResource.setFileName(originalFilename);
        fileResource.setFileType(fileType);
        fileResource.setFileSize(file.getSize());
        fileResource.setFileUrl(Paths.get(directory, fileName).toString());
        fileResource.setStatus(FileConstant.STATUS_NORMAL);
        
        // 保存记录
        fileResourceMapper.insert(fileResource);
        
        return fileResource;
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastIndexOf = filename.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return filename.substring(lastIndexOf + 1);
    }
    
    /**
     * 从文件路径中获取文件名
     */
    private String getFileNameFromPath(String path) {
        if (path == null) {
            return "";
        }
        return Paths.get(path).getFileName().toString();
    }
    
    /**
     * 根据文件类型确定文件类型常量
     */
    private String determineFileType(MultipartFile file) {
        String extension = getFileExtension(file.getOriginalFilename()).toLowerCase();
        
        if (Arrays.asList(FileConstant.ALLOWED_IMAGE_EXTENSIONS).contains(extension)) {
            return FileConstant.TYPE_IMAGE;
        } else if (Arrays.asList(FileConstant.ALLOWED_VOICE_EXTENSIONS).contains(extension)) {
            return FileConstant.TYPE_VOICE;
        } else if (Arrays.asList(FileConstant.ALLOWED_VIDEO_EXTENSIONS).contains(extension)) {
            return FileConstant.TYPE_VIDEO;
        } else if (Arrays.asList(FileConstant.ALLOWED_DOCUMENT_EXTENSIONS).contains(extension)) {
            return FileConstant.TYPE_DOCUMENT;
        } else {
            return FileConstant.TYPE_OTHER;
        }
    }
} 