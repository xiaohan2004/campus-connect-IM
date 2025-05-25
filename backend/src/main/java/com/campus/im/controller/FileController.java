package com.campus.im.controller;

import com.campus.im.entity.FileResource;
import com.campus.im.common.Result;
import com.campus.im.common.enumeration.ResultCode;
import com.campus.im.service.FileResourceService;
import com.campus.im.util.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 文件控制器
 */
@RestController
@RequestMapping("/api/file")
public class FileController {

    @Autowired
    private FileResourceService fileResourceService;

    /**
     * 上传文件
     *
     * @param file 文件
     * @param type 文件类型：image、video、audio、document
     * @param request HTTP请求
     * @return 上传结果
     */
    @PostMapping("/upload")
    public Result uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "document") String type,
            HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        if (file.isEmpty()) {
            return Result.error(ResultCode.PARAM_ERROR, "文件不能为空");
        }

        try {
            FileResource fileResource;
            switch (type) {
                case "image":
                    fileResource = fileResourceService.uploadImage(file, userId);
                    break;
                case "video":
                    fileResource = fileResourceService.uploadVideo(file, userId, null, null, null);
                    break;
                case "audio":
                    fileResource = fileResourceService.uploadVoice(file, userId, null);
                    break;
                default:
                    fileResource = fileResourceService.uploadFile(file, userId);
                    break;
            }
            return Result.success(fileResource);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件
     *
     * @param fileId 文件ID
     * @param response HTTP响应
     * @return 文件资源
     */
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId, HttpServletResponse response) {
        try {
            FileResource fileResource = fileResourceService.getFileResource(fileId);
            if (fileResource == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在");
                return null;
            }

            Resource resource = null; // 临时处理，实际应调用 fileResourceService 的方法
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileResource.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileResource.getFileName() + "\"")
                    .body(resource);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 获取文件信息
     *
     * @param fileId 文件ID
     * @return 文件信息
     */
    @GetMapping("/{fileId}")
    public Result getFileInfo(@PathVariable Long fileId) {
        FileResource fileResource = fileResourceService.getFileResource(fileId);
        if (fileResource == null) {
            return Result.error(ResultCode.NOT_FOUND, "文件不存在");
        }
        return Result.success(fileResource);
    }

    /**
     * 删除文件
     *
     * @param fileId 文件ID
     * @param request HTTP请求
     * @return 删除结果
     */
    @DeleteMapping("/{fileId}")
    public Result deleteFile(@PathVariable Long fileId, HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        boolean success = fileResourceService.deleteFile(fileId, userId);
        if (success) {
            return Result.success();
        } else {
            return Result.error("删除文件失败，可能没有权限");
        }
    }

    /**
     * 获取用户上传的文件列表
     *
     * @param request HTTP请求
     * @return 文件列表
     */
    @GetMapping("/my")
    public Result getUserFiles(HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        List<FileResource> files = fileResourceService.getUserFileResources(userId);
        return Result.success(files);
    }

    /**
     * 获取群文件列表
     *
     * @param groupId 群组ID
     * @return 文件列表
     */
    @GetMapping("/group/{groupId}")
    public Result getGroupFiles(@PathVariable Long groupId) {
        return Result.error(ResultCode.SERVICE_UNAVAILABLE, "功能未实现");
    }

    /**
     * 上传群文件
     *
     * @param file 文件
     * @param groupId 群组ID
     * @param request HTTP请求
     * @return 上传结果
     */
    @PostMapping("/group/{groupId}/upload")
    public Result uploadGroupFile(
            @RequestParam("file") MultipartFile file,
            @PathVariable Long groupId,
            HttpServletRequest request) {
        Long userId = AuthUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        if (file.isEmpty()) {
            return Result.error(ResultCode.PARAM_ERROR, "文件不能为空");
        }

        try {
            return Result.error(ResultCode.SERVICE_UNAVAILABLE, "功能未实现");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
}