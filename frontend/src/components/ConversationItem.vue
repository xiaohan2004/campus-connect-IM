<template>
  <div 
    class="conversation-item" 
    :class="{ 'active': isActive, 'unread': hasUnread }"
    @click="selectConversation"
  >
    <!-- 对话头像 -->
    <div class="avatar">
      <img :src="avatarUrl" alt="avatar" />
      <div v-if="conversation.online" class="online-indicator"></div>
    </div>

    <!-- 对话信息 -->
    <div class="info">
      <div class="top-line">
        <div class="name">{{ displayName }}</div>
        <div class="time">{{ formatTime(conversation.lastMessage?.timestamp) }}</div>
      </div>
      <div class="bottom-line">
        <div class="last-message">
          <span v-if="conversation.lastMessage">
            <span v-if="conversation.lastMessage.contentType === 'text'">
              {{ conversation.lastMessage.content }}
            </span>
            <span v-else-if="conversation.lastMessage.contentType === 'image'">
              [图片]
            </span>
            <span v-else>
              [未知消息类型]
            </span>
          </span>
          <span v-else class="no-message">暂无消息</span>
        </div>
        <div v-if="hasUnread" class="unread-count">
          {{ conversation.unreadCount > 99 ? '99+' : conversation.unreadCount }}
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { computed } from 'vue';
import { formatTime } from '@/utils/format';

export default {
  name: 'ConversationItem',
  props: {
    conversation: {
      type: Object,
      required: true
    },
    isActive: {
      type: Boolean,
      default: false
    }
  },
  setup(props, { emit }) {
    const displayName = computed(() => {
      if (props.conversation.type === 'private') {
        return props.conversation.target?.nickname || '未知用户';
      } else {
        return props.conversation.name || '未命名群组';
      }
    });

    const avatarUrl = computed(() => {
      if (props.conversation.type === 'private') {
        return props.conversation.target?.avatar || '/src/assets/default-avatar.png';
      } else {
        return props.conversation.avatar || '/src/assets/default-group.png';
      }
    });

    const hasUnread = computed(() => {
      return props.conversation.unreadCount > 0;
    });

    const selectConversation = () => {
      emit('select', props.conversation);
    };

    return {
      displayName,
      avatarUrl,
      hasUnread,
      formatTime,
      selectConversation
    };
  }
};
</script>

<style lang="scss" scoped>
.conversation-item {
  display: flex;
  padding: 12px 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: background-color 0.2s;
  
  &:hover {
    background-color: rgba(0, 0, 0, 0.05);
  }
  
  &.active {
    background-color: rgba(0, 122, 255, 0.1);
  }
  
  &.unread {
    .name {
      font-weight: 600;
    }
  }
  
  .avatar {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    overflow: hidden;
    position: relative;
    flex-shrink: 0;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    
    .online-indicator {
      position: absolute;
      bottom: 2px;
      right: 2px;
      width: 12px;
      height: 12px;
      background-color: #34c759;
      border-radius: 50%;
      border: 2px solid white;
    }
  }
  
  .info {
    flex: 1;
    margin-left: 12px;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    justify-content: center;
    
    .top-line {
      display: flex;
      justify-content: space-between;
      margin-bottom: 4px;
      
      .name {
        font-size: 16px;
        font-weight: 500;
        color: #000;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        flex: 1;
      }
      
      .time {
        font-size: 12px;
        color: #8e8e93;
        white-space: nowrap;
        margin-left: 8px;
      }
    }
    
    .bottom-line {
      display: flex;
      justify-content: space-between;
      
      .last-message {
        font-size: 14px;
        color: #8e8e93;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        flex: 1;
        
        .no-message {
          font-style: italic;
        }
      }
      
      .unread-count {
        background-color: #ff3b30;
        color: white;
        font-size: 12px;
        min-width: 18px;
        height: 18px;
        border-radius: 9px;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 0 5px;
        margin-left: 8px;
      }
    }
  }
}
</style> 