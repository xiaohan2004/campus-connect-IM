package com.campus.im.service;

import com.campus.im.entity.User;
import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户信息
     */
    User getUserByPhone(String phone);
    
    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    User getUserById(Long id);
    
    /**
     * 批量查询用户信息
     *
     * @param ids 用户ID列表
     * @return 用户信息列表
     */
    List<User> getUserByIds(List<Long> ids);
    
    /**
     * 创建用户
     *
     * @param user 用户信息
     * @return 是否成功
     */
    boolean createUser(User user);
    
    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @return 是否成功
     */
    boolean updateUser(User user);
    
    /**
     * 更新用户在线状态
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean updateUserOnlineStatus(Long userId);
    
    /**
     * 通过手机号更新用户在线状态
     *
     * @param phone 用户手机号
     * @return 是否成功
     */
    boolean updateUserOnlineStatus(String phone);

    /**
     * 获取所有用户
     *
     * @return 所有用户列表
     */
    List<User> getAllUser();
} 