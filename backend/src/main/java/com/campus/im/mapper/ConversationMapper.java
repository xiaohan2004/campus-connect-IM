package com.campus.im.mapper;

import com.campus.im.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会话Mapper接口
 */
@Mapper
public interface ConversationMapper {
    
    /**
     * 添加会话
     *
     * @param conversation 会话
     * @return 影响行数
     */
    int insert(Conversation conversation);
    
    /**
     * 更新会话
     *
     * @param conversation 会话
     * @return 影响行数
     */
    int update(Conversation conversation);
    
    /**
     * 删除会话（软删除）
     *
     * @param id 会话ID
     * @return 影响行数
     */
    int delete(@Param("id") Long id);
    
    /**
     * 根据ID查询会话
     *
     * @param id 会话ID
     * @return 会话
     */
    Conversation selectById(@Param("id") Long id);
    
    /**
     * 查询用户的会话
     *
     * @param userId 用户ID
     * @param conversationType 会话类型
     * @param targetId 目标ID
     * @return 会话
     */
    Conversation selectByUserIdAndTarget(
            @Param("userId") Long userId,
            @Param("conversationType") Integer conversationType,
            @Param("targetId") Long targetId);
    
    /**
     * 查询用户的会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<Conversation> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 更新会话的未读消息数
     *
     * @param id 会话ID
     * @param count 未读消息数（增量）
     * @return 影响行数
     */
    int updateUnreadCount(@Param("id") Long id, @Param("count") Integer count);
    
    /**
     * 重置会话的未读消息数
     *
     * @param id 会话ID
     * @return 影响行数
     */
    int resetUnreadCount(@Param("id") Long id);
    
    /**
     * 更新会话的最后一条消息
     *
     * @param id 会话ID
     * @param messageId 消息ID
     * @return 影响行数
     */
    int updateLastMessage(@Param("id") Long id, @Param("messageId") Long messageId);
    
    /**
     * 更新会话置顶状态
     *
     * @param id 会话ID
     * @param isTop 是否置顶
     * @return 影响行数
     */
    int updateTopStatus(@Param("id") Long id, @Param("isTop") Integer isTop);
    
    /**
     * 更新会话免打扰状态
     *
     * @param id 会话ID
     * @param isMuted 是否免打扰
     * @return 影响行数
     */
    int updateMuteStatus(@Param("id") Long id, @Param("isMuted") Integer isMuted);
} 