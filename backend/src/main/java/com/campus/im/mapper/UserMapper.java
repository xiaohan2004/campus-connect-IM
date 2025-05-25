package com.campus.im.mapper;

import com.campus.im.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper {
    
    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户信息
     */
    User selectByPhone(@Param("phone") String phone);
    
    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    User selectById(@Param("id") Long id);
    
    /**
     * 批量查询用户信息
     *
     * @param ids 用户ID列表
     * @return 用户信息列表
     */
    List<User> selectByIds(@Param("ids") List<Long> ids);
    
    /**
     * 插入用户信息
     *
     * @param user 用户信息
     * @return 影响行数
     */
    int insert(User user);
    
    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @return 影响行数
     */
    int update(User user);
    
    /**
     * 更新用户在线状态
     *
     * @param id 用户ID
     * @return 影响行数
     */
    int updateLastActiveTime(@Param("id") Long id);
} 