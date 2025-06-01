package com.campus.im.service.impl;

import com.campus.im.entity.User;
import com.campus.im.mapper.UserMapper;
import com.campus.im.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public User getUserByPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return null;
        }
        return userMapper.selectByPhone(phone);
    }
    
    @Override
    public User getUserById(Long id) {
        if (id == null) {
            return null;
        }
        return userMapper.selectById(id);
    }
    
    @Override
    public List<User> getUserByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return userMapper.selectByIds(ids);
    }
    
    @Override
    @Transactional
    public boolean createUser(User user) {
        if (user == null) {
            return false;
        }
        
        // 检查手机号是否存在
        User existUser = getUserByPhone(user.getPhone());
        if (existUser != null) {
            return false;
        }
        
        return userMapper.insert(user) > 0;
    }
    
    @Override
    @Transactional
    public boolean updateUser(User user) {
        if (user == null || user.getId() == null) {
            return false;
        }
        
        return userMapper.update(user) > 0;
    }
    
    @Override
    public boolean updateUserOnlineStatus(Long userId) {
        if (userId == null) {
            return false;
        }
        
        return userMapper.updateLastActiveTime(userId) > 0;
    }
    
    @Override
    public boolean updateUserOnlineStatus(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        
        // 先通过手机号查询用户ID
        User user = getUserByPhone(phone);
        if (user == null) {
            return false;
        }
        
        // 使用用户ID更新在线状态
        return updateUserOnlineStatus(user.getId());
    }

    @Override
    public List<User> getAllUser() {
        return userMapper.selectAll();
    }
} 