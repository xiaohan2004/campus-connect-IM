package com.campus.im.mapper;

import com.campus.im.entity.FileResource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文件资源Mapper接口
 */
@Mapper
public interface FileResourceMapper {
    
    /**
     * 添加文件资源
     *
     * @param fileResource 文件资源
     * @return 影响行数
     */
    int insert(FileResource fileResource);
    
    /**
     * 更新文件资源
     *
     * @param fileResource 文件资源
     * @return 影响行数
     */
    int update(FileResource fileResource);
    
    /**
     * 删除文件资源（更新状态）
     *
     * @param id 资源ID
     * @return 影响行数
     */
    int delete(@Param("id") Long id);
    
    /**
     * 根据ID查询文件资源
     *
     * @param id 资源ID
     * @return 文件资源
     */
    FileResource selectById(@Param("id") Long id);
    
    /**
     * 根据用户ID查询文件资源
     *
     * @param userId 用户ID
     * @return 文件资源列表
     */
    List<FileResource> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 根据文件类型查询文件资源
     *
     * @param userId 用户ID
     * @param fileType 文件类型
     * @return 文件资源列表
     */
    List<FileResource> selectByUserIdAndFileType(@Param("userId") Long userId, @Param("fileType") String fileType);
} 